package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseBdStorage<Mpa> {
    private static final String GET_MPA_RATING_BY_ID =
            "SELECT * FROM mpaRatings " +
                    "WHERE id = ?;";
    private static final String GET_ALL_MPA_RATINGS =
            "SELECT * FROM mpaRatings ORDER BY id;";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    public Mpa getMpaById(int mpaId) {
        Optional<Mpa> optMpa = findOne(GET_MPA_RATING_BY_ID, mpaId);
        if (optMpa.isEmpty()) {
            throw new NoSuchElementException("Рейтинга с id=" + mpaId + "не в БД");
        }
        return optMpa.get();
    }

    public List<Mpa> getAllMpa() {
        return findMany(GET_ALL_MPA_RATINGS);
    }
}
