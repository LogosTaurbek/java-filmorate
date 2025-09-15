package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    private int currentId = 0;


    public User createUser(User newUser) {
        User validatedUser = UserService.validateUser(newUser);
        newUser.setId(this.getNextId());
        this.users.put(validatedUser.getId(), validatedUser);
        log.info("Создан пользователь: {}", newUser);
        return validatedUser;
    }

    public User updateUser(User updatedUser) {

        User validatedUser = UserService.validateUser(updatedUser);
        this.users.put(validatedUser.getId(), validatedUser);
        log.info("Обновлены данные о пользователе: {}", updatedUser);
        return validatedUser;
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