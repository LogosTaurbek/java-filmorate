package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}