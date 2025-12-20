package ru.yandex.practicum.filmorate.exceptions;

public class DuplicatedDataException extends RuntimeException {
    public DuplicatedDataException(String msg) {
        super(msg);
    }
}
