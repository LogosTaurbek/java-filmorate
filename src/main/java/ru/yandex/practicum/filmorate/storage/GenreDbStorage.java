package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseBdStorage<Genre> implements GenreStorage {

    private static final String GET_GENRE_BY_ID =
            "SELECT * FROM genres " +
                    "WHERE id = ?;";
    private static final String GET_ALL_GENRES =
            "SELECT * FROM genres ORDER BY id;";
    private static final String CHECK_FOR_GENRE_QUERY =
            "SELECT COUNT(*) FROM genres " +
                    "WHERE id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Genre> getGenreById(int genreId) {
        Optional<Genre> optGenre = findOne(GET_GENRE_BY_ID, genreId);
        return optGenre;
    }

    @Override
    public boolean genreExists(int genreId) {
        Integer numGenresFound = jdbc.queryForObject(CHECK_FOR_GENRE_QUERY, Integer.class, genreId);
        return numGenresFound > 0;
    }

    public List<Genre> getAllGenres() {
        return findMany(GET_ALL_GENRES);
    }
}
