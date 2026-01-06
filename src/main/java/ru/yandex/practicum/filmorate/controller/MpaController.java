package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping("/{mpaId}")
    @ResponseStatus(HttpStatus.OK)
    public Mpa getMpaById(@PathVariable int mpaId) {
        log.info("Запрос на получение рейтинга с id=" + mpaId);
        return this.mpaService.getMpaById(mpaId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<Mpa> getAllMpa() {
        log.info("Запрос на получение всех рейтингов");
        return this.mpaService.getAllMpa();
    }
}
