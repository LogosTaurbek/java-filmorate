package ru.yandex.practicum.filmorate.model;


import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.exceptions.UserValidationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<User> usersWhoLiked;

    public Film(int id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.usersWhoLiked = new HashSet<>();
    }

    public void addLike(User user) {
        if (user == null) {
            throw new UserValidationException("Пользователь не может быть null.");
        }
        this.usersWhoLiked.add(user);
    }

    public void removeLike(User user) {
        if (user == null) {
            throw new UserValidationException("Пользователь не может быть null.");
        }
        this.usersWhoLiked.remove(user);
    }

    public int getNumberOfLikes() {
        return this.usersWhoLiked.size();
    }
}
