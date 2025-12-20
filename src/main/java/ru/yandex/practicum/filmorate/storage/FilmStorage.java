package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film newFilm);

    Film updateFilm(Film updatedFilm);

    List<Film> getAllFilms();

    boolean filmExists(String name, LocalDate releaseDate, int duration);

    Optional<Film> getFilmById(int id);

    boolean mpaRatingExists(int ratingId);

    boolean genreExists(int genreId);

    void addLike(int userId, int filmId);

    void removeLike(int userId, int filmId);

    List<Integer> getTopLikedFilmIds(int count);
}
