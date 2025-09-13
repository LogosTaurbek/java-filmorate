package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        return this.filmStorage.getAllFilms();
    }

    public Film getFilmById(int id) {
        return this.filmStorage.getFilmById(id);
    }

    public Film addFilm(Film newFilm) {
        return this.filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film updatedFilm) {
        return this.filmStorage.updateFilm(updatedFilm);
    }
}
