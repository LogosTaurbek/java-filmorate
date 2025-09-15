package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmValidationTests {
    FilmStorage filmStorage;
    FilmService filmService;
    FilmController filmController;
    Film testFilm;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(filmStorage);
        filmController = new FilmController(filmService);
        testFilm = new Film(0, "", "", LocalDate.of(2000, 1, 1), 0);
        testFilm.setId(1);
        testFilm.setName("Name");
        testFilm.setDescription("Description");
        testFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        testFilm.setDuration(50);
    }

    @Test
    void testValidFilm() {
        assertDoesNotThrow(() -> filmController.addFilm(testFilm));
    }

    @Test
    void testNoName() {
        testFilm.setName("");
        assertThrows(FilmValidationException.class, () -> filmController.addFilm(testFilm));
    }

    @Test
    void testLongDescription() {
        testFilm.setDescription("a".repeat(201));
        assertThrows(FilmValidationException.class, () -> filmController.addFilm(testFilm));
    }

    @Test
    void testEarlyReleaseDate() {
        testFilm.setReleaseDate(LocalDate.of(1894, 12, 28));
        assertThrows(FilmValidationException.class, () -> filmController.addFilm(testFilm));
    }

    @Test
    void testNegativeDuration() {
        testFilm.setDuration(-1);
        assertThrows(FilmValidationException.class, () -> filmController.addFilm(testFilm));
    }
}
