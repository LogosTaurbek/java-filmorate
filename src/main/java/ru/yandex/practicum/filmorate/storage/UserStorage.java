package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User newUser);

    User updateUser(User updatedUser);

    List<User> getAllUsers();

    Optional<User> getUserById(int userId);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByLogin(String login);

    List<User> getUserFriends(int userId);

    void addFriend(int userId, int friendId);

    void removeUser(int userId);

    void removeFriend(int userId, int friendId);
}
