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

@Repository
public class FilmDbStorage extends BaseBdStorage<Film> implements FilmStorage {
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
            "SELECT * from films WHERE id = ?;";
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

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

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
        List<Genre> genres = new ArrayList<>();
        if (newFilm.getGenres() != null) {
            genres = new ArrayList<>(new LinkedHashSet<>(newFilm.getGenres()));
        }
        List<Object[]> batchedFilmIdGenreIds = new ArrayList<>();
        if (!genres.isEmpty()) {
            for (Genre genre : genres) {
                if (!genreExists(genre.getId())) {
                    throw new NoSuchElementException("Жанра с ID=" + genre.getId() + " нет в БД.");
                }
                batchedFilmIdGenreIds.add(new Object[]{filmId, genre.getId()});
            }
        }
        jdbc.batchUpdate(INSERT_GENRE_QUERY, batchedFilmIdGenreIds);
        if (newFilm.getMpa() != null) {
            insertWithoutGeneratedId(INSERT_MPA_QUERY, filmId, newFilm.getMpa().getId());
        }
        return newFilm;
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
        List<Film> films = findMany(GET_ALL_FILMS);
        for (Film film : films) {
            film.setMpa(getFilmMpaRating(film.getId()));
            film.setGenres(getFilmGenres(film.getId()));
        }
        return films;
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        Optional<Film> optFilm = findOne(GET_FILM_BY_ID, filmId);
        if (optFilm.isEmpty()) {
            return optFilm;
        }
        Film film = optFilm.get();
        film.setMpa(getFilmMpaRating(film.getId()));
        film.setGenres(getFilmGenres(film.getId()));
        return Optional.of(film);
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
    public boolean genreExists(int genreId) {
        Integer numGenresFound = jdbc.queryForObject(CHECK_FOR_GENRE_QUERY, Integer.class, genreId);
        return numGenresFound > 0;
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
