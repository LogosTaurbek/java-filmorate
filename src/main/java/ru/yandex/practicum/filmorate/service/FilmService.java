package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.config.AppConfig;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.FilmValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final AppConfig appConfig;
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(
            AppConfig appConfig,
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            UserService userService
    ) {
        this.appConfig = appConfig;
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<FilmDto> getAllFilms() {
        log.info("Выведен весь список фильмов");
        return this.filmStorage.getAllFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto addFilm(NewFilmRequest newFilmRequest) {
        validateFilm(newFilmRequest);
        log.info("Добавлен фильм: {}", newFilmRequest);
        Film newFilm = FilmMapper.mapToFilm(newFilmRequest);
        newFilm = filmStorage.addFilm(newFilm);
        return FilmMapper.mapToFilmDto(newFilm);
    }

    public FilmDto updateFilm(UpdateFilmRequest updateFilmRequest) throws NoSuchElementException {
        Film filmExc = this.getFilmByIdWithException(updateFilmRequest.getId());
        Film updatedFilm = FilmMapper.updateFilmFields(filmExc, updateFilmRequest);
        updatedFilm = filmStorage.updateFilm(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public Film getFilmById(int id) {
        log.info("Получение фильма с id=" + id);
        return getFilmByIdWithException(id);
    }

    public void validateFilm(NewFilmRequest film) throws FilmValidationException {
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
        if (film.getMpa() != null) {
            Mpa mpaRating = film.getMpa();
            if (!filmStorage.mpaRatingExists(mpaRating.getId())) {
                throw new NoSuchElementException("В БД нет рейтинга MPA с ID=" + mpaRating.getId());
            }
        }
        if (film.getGenres() != null) {
            List<Genre> genres = film.getGenres();
            for (Genre genre : genres) {
                if (!filmStorage.genreExists(genre.getId())) {
                    throw new NoSuchElementException("В БД нет жанра с ID=" + genre.getId());
                }
            }
        }
        log.info("Валидация фильма с названием = " + film.getName());
    }

    public void addLike(int userId, int filmId) {
        filmStorage.addLike(userId, filmId);
    }

    public void removeLike(int userId, int filmId) {
        filmStorage.removeLike(userId, filmId);
    }

    public List<FilmDto> getTopLikedFilms(Integer count) {
        if (count == null) {
            count = appConfig.getDefaultNumberOfTopFilms();
        }
        return filmStorage.getTopLikedFilmIds(count).stream()
                .map(filmId -> filmStorage.getFilmById(filmId).get())
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private Film getFilmByIdWithException(int id) {
        Optional<Film> optFilm = filmStorage.getFilmById(id);
        if (optFilm.isEmpty()) {
            throw new NoSuchElementException("Фильма с ID=" + id + "нет в БД.");
        }
        return optFilm.get();
    }

}
