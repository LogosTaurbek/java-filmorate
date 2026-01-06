package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateFilmRequest {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private List<Genre> genres;
    private Mpa mpa;

    public boolean hasName() {
        return !(this.name == null || this.name.isBlank());
    }

    public boolean hasDescription() {
        return !(this.description == null || this.description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(this.releaseDate == null);
    }

    public boolean hasDuration() {
        return !(this.duration == null);
    }

    public boolean hasGenres() {
        return !(this.genres == null);
    }

    public boolean hasMpa() {
        return !(this.mpa == null);
    }
}
