package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.exceptions.UserValidationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Integer> friendIds;

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friendIds = new HashSet<>();
    }

    public void addFriend(User newFriend) {
        if (newFriend == null) {
            throw new UserValidationException("Пользователь не может быть null.");
        }
        this.friendIds.add(newFriend.getId());
    }

    public Integer getId() {
        return id;
    }

    public void removeFriend(User user) {
        this.friendIds.remove(user.getId());
    }
}
