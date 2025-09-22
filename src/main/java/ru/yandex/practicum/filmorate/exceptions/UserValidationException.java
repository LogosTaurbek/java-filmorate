package ru.yandex.practicum.filmorate.exceptions;

public class UserValidationException extends RuntimeException {
    public UserValidationException(String msg) {
        super(msg);
    }
}
