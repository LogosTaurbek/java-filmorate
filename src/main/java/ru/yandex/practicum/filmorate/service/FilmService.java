package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.config.AppConfig;
import ru.yandex.practicum.filmorate.exceptions.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {

    private final AppConfig appConfig;
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(AppConfig appConfig, FilmStorage filmStorage, UserService userService) {
        this.appConfig = appConfig;
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Optional<List<Film>> getAllFilms() {
        log.info("Выведен весь список фильмов");
        return this.filmStorage.getAllFilms();
    }

    public Film addFilm(Film newFilm) {
        validateFilm(newFilm);
        log.info("Добавлен фильм: {}", newFilm);
        return this.filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film updatedFilm) throws NoSuchElementException {
        if (!this.isFilmExist(updatedFilm.getId())) {
            throw new NoSuchElementException("Фильма с id=" + updatedFilm.getId() + " нет в системе.");
        } else {
            log.info("Редактирование фильма с id=" + updatedFilm);
            validateFilm(updatedFilm);
            return this.filmStorage.updateFilm(updatedFilm);
        }
    }

    public Film getFilmById(int id) {
        return getFilmByIdWithException(id);
    }

    public boolean isFilmExist(int filmId) {
        log.info("Проверка существование фильма с id=" + filmId);
        return this.getFilmByIdWithException(filmId) != null;
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
        log.info("Валидация фильма с названием = " + film.getName());
    }

    public void addLike(int userId, int filmId) {
        User user = this.userService.getUserById(userId);
        log.info("Добавление лайка пользователем с id=" + userId + " к фильму с id=" + filmId);
        Film film = this.getFilmByIdWithException(filmId);
        film.addLike(user);
    }

    public void removeLike(int userId, int filmId) {
        User user = this.userService.getUserById(userId);
        log.info("Удаление лайка пользователем с id=" + userId + " к фильму с id=" + filmId);
        Film film = this.getFilmByIdWithException(filmId);
        film.removeLike(user);
    }

    public List<Film> getTopLikedFilms(Integer count) {
        log.info("Получение списка топ фильмов с количеством лайков " + count);
        if (count == null || count <= 0) {
            count = appConfig.getDefaultNumberOfTopFilms();
        }
        List<Film> films = this.filmStorage.getAllFilms().get();
        return films
                .stream()
                .sorted((f1, f2) -> f2.getNumberOfLikes() - f1.getNumberOfLikes())
                .limit(count)
                .toList();
    }

    private Film getFilmByIdWithException(int id) {
        Film film = this.filmStorage.getFilmById(id);
        log.info("Получение фильма с id=" + id);
        if (film == null) {
            throw new NoSuchElementException("Фильма с id=" + id + " не существует.");
        }
        return film;
    }

}
