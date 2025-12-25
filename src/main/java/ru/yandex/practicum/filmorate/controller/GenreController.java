package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/{genreId}")
    @ResponseStatus(HttpStatus.OK)
    public Genre getGenreById(@PathVariable int genreId) {
        return this.genreService.getGenreById(genreId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<Genre> getAllGenres() {
        return this.genreService.getAllGenres();
    }
}
