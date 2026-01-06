package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage extends BaseBdStorage<Film> implements FilmStorage {


    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreStorage genreStorage) {
        super(jdbc, mapper);
    }

    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films(name, description, releaseDate, duration) " +
                    "VALUES (?, ?, ?, ?)";
    private static final String INSERT_GENRE_QUERY =
            "INSERT INTO filmsGenres(filmId, genreId) " +
                    "VALUES (?, ?)";
    private static final String INSERT_MPA_QUERY =
            "INSERT INTO filmsMpaRatings(filmId, ratingId) " +
                    "VALUES (?, ?)";
    private static final String CHECK_FOR_MPA_QUERY =
            "SELECT COUNT(*) FROM mpaRatings " +
                    "WHERE id = ?";
    private static final String CHECK_FOR_GENRE_QUERY =
            "SELECT COUNT(*) FROM genres " +
                    "WHERE id = ?";
    private static final String CHECK_FOR_FILM_QUERY =
            "SELECT COUNT(*) FROM films " +
                    "WHERE name = ? " +
                    "AND releaseDate = ? " +
                    "AND duration = ?";
    private static final String GET_FILM_BY_ID =
            "SELECT f.*, m.ratingId as mpa_id, r.id as rating_id, r.rating as rating_name, " +
                    "g.id as genre_id, g.genre as genre_name " +
                    "FROM films f " +
                    "LEFT JOIN filmsMpaRatings m ON m.filmId = f.id " +
                    "LEFT JOIN mpaRatings r ON r.id = m.ratingId " +
                    "LEFT JOIN filmsGenres fg ON fg.filmId = f.id " +
                    "LEFT JOIN genres g ON g.id = fg.genreId " +
                    "WHERE f.id = ?;";
    private static final String GET_FILMS_BY_IDS =
            "SELECT f.*, m.ratingId as mpa_id, r.id as rating_id, r.rating as rating_name, " +
                    "g.id as genre_id, g.genre as genre_name " +
                    "FROM films f " +
                    "LEFT JOIN filmsMpaRatings m ON m.filmId = f.id " +
                    "LEFT JOIN mpaRatings r ON r.id = m.ratingId " +
                    "LEFT JOIN filmsGenres fg ON fg.filmId = f.id " +
                    "LEFT JOIN genres g ON g.id = fg.genreId " +
                    "WHERE f.id IN (%s);";
    private static final String GET_MPA_RATING_BY_FILM_ID =
            "SELECT ratingId from filmsMpaRatings WHERE filmId = ?;";
    private static final String GET_MPA_RATING_BY_ID =
            "SELECT rating from MpaRatings WHERE id = ?;";
    private static final String GET_GENRES_BY_FILM_ID =
            "SELECT genreId from filmsGenres WHERE filmId = ?;";
    private static final String GET_GENRES_BY_ID =
            "SELECT genre from genres WHERE id = ?;";
    private static final String UPDATE_FILM =
            "UPDATE films " +
                    "SET name = ?, description = ?, releaseDate = ?, duration = ? " +
                    "WHERE id = ?;";
    private static final String UPDATE_MPA_RATING =
            "UPDATE filmsMpaRatings " +
                    "SET ratingId = ? " +
                    "WHERE filmId = ?;";
    private static final String DELETE_GENRES_BY_FILM_ID =
            "DELETE from filmsGenres WHERE filmId = ?;";
    private static final String GET_ALL_FILMS =
            "SELECT * from films;";
    private static final String GET_ALL_FILMS_WITH_RATING =
            "SELECT f.*, m.ratingId as mpa_id, r.id as rating_id, r.rating as rating_name, " +
                    "g.id as genre_id, g.genre as genre_name " +
                    "FROM films f " +
                    "LEFT JOIN filmsMpaRatings m ON m.filmId = f.id " +
                    "LEFT JOIN MpaRatings r ON r.id = m.ratingId " +
                    "LEFT JOIN filmsGenres fg ON fg.filmId = f.id " +
                    "LEFT JOIN genres g ON g.id = fg.genreId;";
    private static final String ADD_LIKE =
            "INSERT INTO likes(userId, filmId) " +
                    "VALUES (?, ?);";
    private static final String DELETE_LIKE =
            "DELETE from likes WHERE userId = ? AND filmId = ?;";
    private static final String GET_TOP_LIKED_FILMS =
            "SELECT filmId " +
                    "FROM likes " +
                    "GROUP BY filmId " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT ?;";

    @Override
    public Film addFilm(Film newFilm) {
        int filmId = insert(
                INSERT_FILM_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration()
        );
        newFilm.setId(filmId);

        return newFilm;
    }

    public void insertFilmsGenres(List<Object[]> batchedFilmIdGenreIds) {
        jdbc.batchUpdate(INSERT_GENRE_QUERY, batchedFilmIdGenreIds);
    }

    public void insertFilmsMpa(int filmId, int filmMpaId) {
        insertWithoutGeneratedId(INSERT_MPA_QUERY, filmId, filmMpaId);
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        update(
                UPDATE_FILM,
                updatedFilm.getName(),
                updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(),
                updatedFilm.getDuration(),
                updatedFilm.getId()
        );
        if (updatedFilm.getMpa() != null) {
            update(
                    UPDATE_MPA_RATING,
                    updatedFilm.getMpa().getId(),
                    updatedFilm.getId()
            );
        }
        if (updatedFilm.getGenres() != null) {
            int rowsDeleted = jdbc.update(DELETE_GENRES_BY_FILM_ID, updatedFilm.getId());
            List<Genre> genres = updatedFilm.getGenres();
            for (Genre genre : genres) {
                insertWithoutGeneratedId(INSERT_GENRE_QUERY, updatedFilm.getId(), genre.getId());
            }
        }
        return updatedFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = findMany(GET_ALL_FILMS_WITH_RATING);
        Map<Integer, Film> filmsMap = new LinkedHashMap<>();

        for (Film film : films) {
            Film existingFilm = filmsMap.get(film.getId());

            if (existingFilm == null) {
                filmsMap.put(film.getId(), film);
            } else {
                if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                    existingFilm.getGenres().addAll(film.getGenres());
                }
            }
        }
        return films;
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        List<Film> rawFilms = findMany(GET_FILM_BY_ID, filmId);

        if (rawFilms.isEmpty()) {
            return Optional.empty();
        }

        Film film = rawFilms.get(0);
        for (int i = 1; i < rawFilms.size(); i++) {
            Film nextFilm = rawFilms.get(i);
            if (nextFilm.getGenres() != null && !nextFilm.getGenres().isEmpty()) {
                film.getGenres().addAll(nextFilm.getGenres());
            }
        }
        return Optional.of(film);
    }

    @Override
    public List<Film> getFilmsByIds(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(GET_FILMS_BY_IDS, inClause);

        List<Film> rawFilms = findMany(sql, filmIds.toArray());

        if (rawFilms.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Film> filmsMap = rawFilms.stream()
                .collect(Collectors.toMap(
                        Film::getId,
                        film -> film,
                        (existing, newFilm) -> {
                            if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
                                existing.getGenres().addAll(newFilm.getGenres());
                            }
                            return existing;
                        },
                        LinkedHashMap::new
                ));

        return new ArrayList<>(filmsMap.values());
    }

    @Override
    public boolean filmExists(String name, LocalDate releaseDate, int duration) {
        Integer numFilmsFound = jdbc.queryForObject(CHECK_FOR_FILM_QUERY, Integer.class, name, releaseDate, duration);
        return numFilmsFound > 0;
    }

    @Override
    public boolean mpaRatingExists(int ratingId) {
        Integer numMpaFound = jdbc.queryForObject(CHECK_FOR_MPA_QUERY, Integer.class, ratingId);
        return numMpaFound > 0;
    }

    @Override
    public void addLike(int userId, int filmId) {
        insertWithoutGeneratedId(ADD_LIKE, userId, filmId);
    }

    @Override
    public void removeLike(int userId, int filmId) {
        int rowsDeleted = jdbc.update(DELETE_LIKE, userId, filmId);
    }

    @Override
    public List<Integer> getTopLikedFilmIds(int count) {
        return jdbc.queryForList(GET_TOP_LIKED_FILMS, Integer.class, count);
    }

    private Mpa getFilmMpaRating(int filmId) {
        int mpaRatingId;
        try {
            mpaRatingId = jdbc.queryForObject(GET_MPA_RATING_BY_FILM_ID, Integer.class, filmId);
        } catch (DataAccessException e) {
            return null;
        }
        String mpaRatingName = jdbc.queryForObject(GET_MPA_RATING_BY_ID, String.class, mpaRatingId);
        Mpa mpa = new Mpa();
        mpa.setId(mpaRatingId);
        mpa.setName(mpaRatingName);
        return mpa;
    }

    private List<Genre> getFilmGenres(int filmId) {
        List<Integer> genreIds;
        try {
            genreIds = jdbc.queryForList(GET_GENRES_BY_FILM_ID, Integer.class, filmId);
        } catch (DataAccessException e) {
            return null;
        }
        List<Genre> genres = new ArrayList<>();
        for (int genreId : genreIds) {
            Genre genre = new Genre();
            genre.setId(genreId);
            String genreName = jdbc.queryForObject(GET_GENRES_BY_ID, String.class, genreId);
            genre.setName(genreName);
            genres.add(genre);
        }
        return genres;
    }
}
