package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(LocalDate.from(
                resultSet.getTimestamp("releaseDate").toLocalDateTime())
        );
        film.setDuration(resultSet.getInt("duration"));

        // Собираем MPA объект
        try {
            int ratingId = resultSet.getInt("rating_id");
            if (!resultSet.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(ratingId);
                mpa.setName(resultSet.getString("rating_name"));
                film.setMpa(mpa);
            }
        } catch (Exception e) {
            // Колонок для MPA нет
        }

        // Собираем жанры
        try {
            int genreId = resultSet.getInt("genre_id");
            if (!resultSet.wasNull()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(resultSet.getString("genre_name"));
                film.setGenres(new ArrayList<>(List.of(genre)));
            } else {
                film.setGenres(new ArrayList<>());
            }
        } catch (Exception e) {
            // Колонок для жанров нет - устанавливаем пустой список
            film.setGenres(new ArrayList<>());
        }
        return film;
    }
}
