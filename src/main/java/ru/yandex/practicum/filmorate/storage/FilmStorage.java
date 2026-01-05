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

    List<Film> getFilmsByIds(List<Integer> filmIds);

    boolean mpaRatingExists(int ratingId);


    void addLike(int userId, int filmId);

    void removeLike(int userId, int filmId);

    List<Integer> getTopLikedFilmIds(int count);
}
