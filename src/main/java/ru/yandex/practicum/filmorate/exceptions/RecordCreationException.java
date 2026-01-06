package ru.yandex.practicum.filmorate.exceptions;

public class RecordCreationException extends RuntimeException {
    public RecordCreationException(String msg) {
        super(msg);
    }
}