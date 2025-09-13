package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    public UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return this.userStorage.getAllUsers();
    }

    public User getUserById(int id) {
        return this.userStorage.getUserById(id);
    }

    public User createUser(User newUser) {
        return this.userStorage.createUser(newUser);
    }

    public User updateUser(User updatedUser) {
        return this.userStorage.updateUser(updatedUser);
    }

}
