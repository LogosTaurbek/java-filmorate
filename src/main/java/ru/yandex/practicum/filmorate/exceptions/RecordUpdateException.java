package ru.yandex.practicum.filmorate.exceptions;

public class RecordUpdateException extends RuntimeException {
    public RecordUpdateException(String msg) {
        super(msg);
    }
}