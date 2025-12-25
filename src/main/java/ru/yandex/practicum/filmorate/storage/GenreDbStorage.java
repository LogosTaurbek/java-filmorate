package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseBdStorage<Genre> {

    private static final String GET_GENRE_BY_ID =
            "SELECT * FROM genres " +
                    "WHERE id = ?;";
    private static final String GET_ALL_GENRES =
            "SELECT * FROM genres ORDER BY id;";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Genre getGenreById(int genreId) {
        Optional<Genre> optGenre = findOne(GET_GENRE_BY_ID, genreId);
        if (optGenre.isEmpty()) {
            throw new NoSuchElementException("Жанра с id=" + genreId + "не в БД");
        }
        return optGenre.get();
    }

    public List<Genre> getAllGenres() {
        return findMany(GET_ALL_GENRES);
    }
}
