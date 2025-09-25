package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film newFilm);

    Film updateFilm(Film updatedFilm);

    Optional<List<Film>> getAllFilms();

    Film getFilmById(int id);
}
