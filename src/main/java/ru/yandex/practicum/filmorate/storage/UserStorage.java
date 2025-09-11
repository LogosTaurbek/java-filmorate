package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User newUser);

    User updateUser(User updatedUser);

    List<User> getAllUsers();

    User getUserById(int id);
}
