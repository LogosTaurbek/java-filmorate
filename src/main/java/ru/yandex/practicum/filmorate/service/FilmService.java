package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.config.AppConfig;
import ru.yandex.practicum.filmorate.exceptions.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class FilmService {

    private final AppConfig appConfig;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(AppConfig appConfig, FilmStorage filmStorage, UserStorage userStorage) {
        this.appConfig = appConfig;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        return this.filmStorage.getAllFilms();
    }

    public Film addFilm(Film newFilm) throws NoSuchElementException {
        validateFilm(newFilm);
        return this.filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film updatedFilm) throws NoSuchElementException {
        if (!this.isFilmExist(updatedFilm.getId())) {
            throw new NoSuchElementException("Пользователя с id=" + updatedFilm.getId() + " нет в системе.");
        } else {
            validateFilm(updatedFilm);
            return this.filmStorage.updateFilm(updatedFilm);
        }
    }

    public Film getFilmById(int id) {
        return this.filmStorage.getFilmById(id);
    }

    public boolean isFilmExist(int filmId) {
        return this.filmStorage.getFilmById(filmId) != null;
    }

    public void validateFilm(Film film) throws FilmValidationException {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма отсутствует. {}", film);
            throw new FilmValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Длина описания фильма ({}) превышает 200 символов. {}", film.getDescription().length(), film);
            throw new FilmValidationException("Максимальная длина описания фильма не может превышать 200 символов.");
        }
        LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(earliestReleaseDate)) {
            log.error("Указана нереалистичная дата релиза. {}", film);
            throw new FilmValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() <= 0) {
            log.error("Указана нереалистичная продолжительность фильма. {}", film);
            throw new FilmValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    public void addLike(int userId, int filmId) {
        User user = this.userStorage.getUserById(userId);
        Film film = this.filmStorage.getFilmById(filmId);
        film.addLike(user);
    }

    public void removeLike(int userId, int filmId) {
        User user = this.userStorage.getUserById(userId);
        Film film = this.filmStorage.getFilmById(filmId);
        film.removeLike(user);
    }

    public List<Film> getTopLikedFilms(Integer count) {
        if (count == null) {
            count = appConfig.getDefaultNumberOfTopFilms();
        }
        return filmStorage.getAllFilms().stream().sorted((f1, f2) -> f2.getNumberOfLikes() - f1.getNumberOfLikes()).limit(count).toList();
    }

}
