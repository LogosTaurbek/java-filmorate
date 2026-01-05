package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    private int currentId = 0;

    public Film addFilm(Film newFilm) {
        newFilm.setId(this.getNextId());
        this.films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public boolean filmExists(String name, LocalDate releaseDate, int duration) {
        return false;
    }


    public Film updateFilm(Film updatedFilm) {
        this.films.put(updatedFilm.getId(), updatedFilm);
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        return new ArrayList<>(this.films.values());
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        return Optional.ofNullable(this.films.get(id));
    }

    @Override
    public List<Film> getFilmsByIds(List<Integer> filmIds) {
        return new ArrayList<>(this.films.values());
    }

    @Override
    public boolean mpaRatingExists(int ratingId) {
        return false;
    }

    @Override
    public void addLike(int userId, int filmId) {
    }

    @Override
    public void removeLike(int userId, int filmId) {
    }

    @Override
    public List<Integer> getTopLikedFilmIds(int count) {
        return List.of();
    }

    private int getNextId() {
        int nextId = this.currentId + 1;
        this.currentId = nextId;
        return nextId;
    }

}
