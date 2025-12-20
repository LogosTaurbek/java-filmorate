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
    public Optional<User> getUserById(int userId) {
        return Optional.ofNullable(this.users.get(userId));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return Optional.empty();
    }


    private int getNextId() {
        int nextId = this.currentId + 1;
        this.currentId = nextId;
        return nextId;
    }

    public void addFriend(int userId, int friendId) {
        User user = this.users.get(userId);
        Set<Integer> friends = user.getFriendIds();
        friends.add(friendId);
        //return user;
    }

    @Override
    public void removeUser(int userId) {
        //return this.users.remove(userId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = this.users.get(userId);
        user.getFriendIds().remove(friendId);
        //return user;
    }

    @Override
    public List<User> getUserFriends(int userId) {
        return new ArrayList<>();
    }
}