package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film newFilm);

    Film updateFilm(Film updatedFilm);

    List<Film> getAllFilms();

    Film getFilmById(int id);
}
