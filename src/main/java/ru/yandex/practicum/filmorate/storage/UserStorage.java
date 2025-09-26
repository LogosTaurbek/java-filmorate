package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User newUser);

    User updateUser(User updatedUser);

    List<User> getAllUsers();

    Optional<User> getUserById(int userId);

    User addFriend(int userId, int friendId);

    User removeUser(int userId);

    User removeFriend(int userId, int friendId);
}
