package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
public class FilmDbStorageTests {

    private final FilmDbStorage filmDbStorage;

    @Test
    public void testAddFilm() {
        Film testFilm = new Film();
        testFilm.setName("Name");
        testFilm.setDescription("Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(60);
        testFilm.setGenres(new ArrayList<>());
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        int idToExpect = addedFilm.getId();
        Optional<Film> filmOptional = filmDbStorage.getFilmById(idToExpect);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", idToExpect)
                );
    }

    @Test
    public void testUpdateFilm() {
        Film testFilm = new Film();
        testFilm.setName("Name");
        testFilm.setDescription("Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(60);
        testFilm.setGenres(new ArrayList<>());
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        int idToExpect = addedFilm.getId();
        testFilm.setId(idToExpect);
        testFilm.setName("Updated Name");
        filmDbStorage.updateFilm(testFilm);
        Optional<Film> filmOptional = filmDbStorage.getFilmById(idToExpect);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "Updated Name")
                );
    }

    @Test
    public void testGetAllFilms() {
// Создаём два фильма
        Film film1 = new Film();
        film1.setName("Name1");
        film1.setDescription("Description1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(60);
        film1.setGenres(new ArrayList<>());
        filmDbStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Name2");
        film2.setDescription("Description2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(120);
        film2.setGenres(new ArrayList<>());
        filmDbStorage.addFilm(film2);

        // Получаем все фильмы
        List<Film> films = filmDbStorage.getAllFilms();

        // Проверяем
        assertThat(films).hasSize(2);

        assertThat(films)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Name1", "Name2");

        assertThat(films)
                .extracting(Film::getDuration)
                .containsExactlyInAnyOrder(60, 120);
    }

    @Test
    public void testFilmExists() {
        Film testFilm = new Film();
        testFilm.setName("Name");
        testFilm.setDescription("Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(60);
        testFilm.setGenres(new ArrayList<>());
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        String nameToExpect = addedFilm.getName();
        LocalDate releaseDateToExpect = addedFilm.getReleaseDate();
        int durationToExpect = addedFilm.getDuration();

        boolean exists = filmDbStorage.filmExists(nameToExpect, releaseDateToExpect, durationToExpect);
        assertThat(exists).isTrue();
    }

    @Test
    public void testMpaRatingExists() {
        boolean exists = filmDbStorage.mpaRatingExists(1);
        assertThat(exists).isTrue();
        exists = filmDbStorage.mpaRatingExists(20);
        assertThat(exists).isFalse();
    }

    @Test
    public void testGenreExists() {
        boolean exists = filmDbStorage.genreExists(1);
        assertThat(exists).isTrue();
        exists = filmDbStorage.genreExists(20);
        assertThat(exists).isFalse();
    }
}
