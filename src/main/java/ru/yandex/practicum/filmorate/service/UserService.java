package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;

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

    public void addFriends(int user1Id, int user2Id) {
        // Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
        // То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.
        User user1 = this.userStorage.getUserById(user1Id);
        User user2 = this.userStorage.getUserById(user2Id);
        user1.addFriend(user2);
        user2.addFriend(user1);
    }

    public void removeFriends(int user1Id, int user2Id) {
        User user1 = this.userStorage.getUserById(user1Id);
        User user2 = this.userStorage.getUserById(user2Id);
        user1.removeFriend(user2);
        user2.removeFriend(user1);
    }

    public List<User> listCommonFriends(int user1Id, int user2Id) {
        Set<Integer> friendsOfUser1 = this.userStorage.getUserById(user1Id).getFriendIds();
        Set<Integer> friendsOfUser2 = this.userStorage.getUserById(user2Id).getFriendIds();
        friendsOfUser1.retainAll(friendsOfUser2);
        return friendsOfUser1.stream().map(userId -> this.userStorage.getUserById(userId)).toList();
    }

    public List<User> getUserFriends(int id) {
        return this.userStorage.getUserById(id).getFriendIds().stream().map(userId -> this.userStorage.getUserById(userId)).toList();
    }
}
