package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Getter
    @Setter
    private int currentId = 0;

    public User createUser(User newUser) {
        User validatedUser = this.validateUser(newUser);
        newUser.setId(this.getNextId());
        this.users.put(validatedUser.getId(), validatedUser);
        log.info("Создан пользователь: {}", newUser);
        return validatedUser;
    }

    public User updateUser(User updatedUser) throws NoSuchElementException {
        if (!this.users.containsKey(updatedUser.getId())) {
            throw new NoSuchElementException("Пользователя с id=" + updatedUser.getId() + " нет в системе.");
        }
        User validatedUser = this.validateUser(updatedUser);
        this.users.put(validatedUser.getId(), validatedUser);
        log.info("Обновлены данные о пользователе: {}", updatedUser);
        return validatedUser;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(this.users.values());
    }

    private int getNextId() {
        int nextId = this.getCurrentId() + 1;
        this.setCurrentId(nextId);
        return nextId;
    }

    // Метод возвращает объект User, потому что в процессе валидации объект может измениться
    private User validateUser(User user) throws UserValidationException {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Отсутствует адрес электронной почты. {}", user);
            throw new UserValidationException("Электронная почта не может быть пустой.");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Адрес электронной почты не содержит \"@\". {}", user);
            throw new UserValidationException("Электронная почта должна содержать символ @.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Отсутствует логин. {}", user);
            throw new UserValidationException("Логин не может быть пустым.");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы. {}", user);
            throw new UserValidationException("Логин не может содержать пробелы.");
        }
        // имя для отображения может быть пустым — в таком случае будет использован логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Отсутствует имя для отображения. Вместо него будет использован логин.");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Указана нереалистичная дата рождения. {}", user);
            throw new UserValidationException("Дата рождения не может быть в будущем.");
        }
        return user;
    }
}
