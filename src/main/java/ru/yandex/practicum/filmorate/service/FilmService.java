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
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final AppConfig appConfig;
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreStorage genreStorage;

    public FilmService(
            AppConfig appConfig,
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            UserService userService,
            GenreStorage genreStorage
    ) {
        this.appConfig = appConfig;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
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

        log.info("Добавление фильма: {}", newFilmRequest);
        Film newFilm = FilmMapper.mapToFilm(newFilmRequest);

        log.info("Добавлен фильм: {}", newFilmRequest);
        newFilm = filmStorage.addFilm(newFilm);
        List<Object[]> batchedFilmIdGenreIds = getBatchedFilmIdsGenresIds(newFilm);

        filmStorage.insertFilmsGenres(batchedFilmIdGenreIds);
        if (newFilm.getMpa() != null) {
            filmStorage.insertFilmsMpa(newFilm.getId(), newFilm.getMpa().getId());
        }
        return FilmMapper.mapToFilmDto(newFilm);
    }

    public FilmDto updateFilm(UpdateFilmRequest updateFilmRequest) throws NoSuchElementException {
        Film filmExc = this.getFilmByIdWithException(updateFilmRequest.getId());
        Film updatedFilm = FilmMapper.updateFilmFields(filmExc, updateFilmRequest);
        updatedFilm = filmStorage.updateFilm(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public FilmDto getFilmById(int id) {
        log.info("Получение фильма с id=" + id);
        Optional<Film> optFilm = filmStorage.getFilmById(id);
        if (optFilm.isEmpty()) {
            throw new NoSuchElementException("Фильма с ID=" + id + "нет в БД.");
        }
        return FilmMapper.mapToFilmDto(optFilm.get());
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
                if (!genreStorage.genreExists(genre.getId())) {
                    throw new NoSuchElementException("В БД нет жанра с ID=" + genre.getId());
                }
            }
        }
        log.info("Валидация фильма с названием = " + film.getName());
    }

    public List<Object[]> getBatchedFilmIdsGenresIds(Film newFilm) {
        log.info("Получение списка жанров фильма с ID=", newFilm.getId());
        List<Genre> genres = new ArrayList<>();
        if (newFilm.getGenres() != null) {
            genres = new ArrayList<>(new LinkedHashSet<>(newFilm.getGenres()));
        }

        List<Object[]> batchedFilmIdGenreIds = new ArrayList<>();
        if (!genres.isEmpty()) {
            for (Genre genre : genres) {
                if (!genreStorage.genreExists(genre.getId())) {
                    throw new NoSuchElementException("Жанра с ID=" + genre.getId() + " нет в БД.");
                }
                batchedFilmIdGenreIds.add(new Object[]{newFilm.getId(), genre.getId()});
            }
        }
        return batchedFilmIdGenreIds;
    }

    public void addLike(int userId, int filmId) {
        log.info("Добавления лайка к фильму с id=" + filmId + " пользователем с id=" + userId);
        Optional<Film> optFilm = filmStorage.getFilmById(filmId);
        if (optFilm.isEmpty()) {
            throw new NoSuchElementException("Фильма с ID=" + filmId + "нет в БД.");
        }
        if (!userService.isUserExist(userId)) {
            throw new NoSuchElementException("Пользователя с id=" + userId + " нет в системе.");
        }
        filmStorage.addLike(userId, filmId);
    }

    public void removeLike(int userId, int filmId) {
        log.info("Удаление лайка с фильма с id=" + filmId + " пользователем с id=" + userId);
        Optional<Film> optFilm = filmStorage.getFilmById(filmId);
        if (optFilm.isEmpty()) {
            throw new NoSuchElementException("Фильма с ID=" + filmId + "нет в БД.");
        }
        if (!userService.isUserExist(userId)) {
            throw new NoSuchElementException("Пользователя с id=" + userId + " нет в системе.");
        }
        filmStorage.removeLike(userId, filmId);
    }

    private Map<Integer, Integer> createOrderMap(List<Integer> ids) {
        Map<Integer, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            orderMap.put(ids.get(i), i);
        }
        return orderMap;
    }

    public List<FilmDto> getTopLikedFilms(Integer count) {
        if (count == null || count <= 0) {
            count = appConfig.getDefaultNumberOfTopFilms();
        }
        List<Integer> topFilmIds = filmStorage.getTopLikedFilmIds(count);
        List<Film> films = filmStorage.getFilmsByIds(topFilmIds);
        Map<Integer, Integer> orderMap = createOrderMap(topFilmIds);

        log.info("Получение списка топ фильмов с количеством лайков " + count);

        return films.stream()
                .sorted(Comparator.comparing(film -> orderMap.get(film.getId())))
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
