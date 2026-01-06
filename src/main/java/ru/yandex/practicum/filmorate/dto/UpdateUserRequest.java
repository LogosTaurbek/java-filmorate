package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public boolean hasEmail() {
        return !(this.email == null || this.email.isBlank());
    }

    public boolean hasLogin() {
        return !(this.login == null || this.login.isBlank());
    }

    public boolean hasName() {
        return !(this.name == null || this.name.isBlank());
    }

    public boolean hasBirthday() {
        return !(this.birthday == null);
    }
}
