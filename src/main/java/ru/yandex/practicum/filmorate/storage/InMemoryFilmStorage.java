package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    private int currentId = 0;

    public Film addFilm(Film newFilm) {
        newFilm.setId(this.getNextId());
        this.films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    public Film updateFilm(Film updatedFilm) {
        this.films.put(updatedFilm.getId(), updatedFilm);
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        return new ArrayList<>(this.films.values());
    }

    @Override
    public Film getFilmById(int id) {
        return this.films.get(id);
    }

    private int getNextId() {
        int nextId = this.currentId + 1;
        this.currentId = nextId;
        return nextId;
    }

}
