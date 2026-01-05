package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa getMpaById(int mpaId) {
        log.info("Получение рейтинга с id=" + mpaId);
        Optional<Mpa> optMpa = mpaStorage.getMpaById(mpaId);
        if (optMpa.isEmpty()) {
            throw new NoSuchElementException("Рейтинга с id=" + mpaId + "нет в БД");
        }
        return mpaStorage.getMpaById(mpaId).get();
    }

    public List<Mpa> getAllMpa() {
        log.info("Выведен весь список рейтингов");
        return mpaStorage.getAllMpa();
    }
}
