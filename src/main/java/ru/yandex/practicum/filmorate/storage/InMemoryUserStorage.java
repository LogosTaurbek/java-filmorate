package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    private int currentId = 0;


    public User createUser(User newUser) {
        newUser.setId(this.getNextId());
        this.users.put(newUser.getId(), newUser);
        return newUser;
    }

    public User updateUser(User updatedUser) {
        this.users.put(updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(this.users.values());
    }

    @Override
    public User getUserById(int userId) {
        return this.users.get(userId);
    }


    private int getNextId() {
        int nextId = this.currentId + 1;
        this.currentId = nextId;
        return nextId;
    }

    public User addFriend(int userId, int friendId) {
        User user = this.users.get(userId);
        Set<Integer> friends = user.getFriendIds();
        friends.add(friendId);
        return user;
    }

    @Override
    public User removeUser(int userId) {
        return this.users.remove(userId);
    }

    public User removeFriend(int userId, int friendId) {
        User user = this.users.get(userId);
        user.getFriendIds().remove(friendId);
        return user;
    }
}