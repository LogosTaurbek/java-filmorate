package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre getGenreById(int genreId) {
        log.info("Получение жанра с id=" + genreId);

        Optional<Genre> optGenre = genreStorage.getGenreById(genreId);
        if (optGenre.isEmpty()) {
            throw new NoSuchElementException("Жанра с id=" + genreId + "не в БД");
        }
        return optGenre.get();
    }

    public List<Genre> getAllGenres() {
        log.info("Выведен весь список жанров");
        return genreStorage.getAllGenres();
    }
}
