package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTests {
    UserStorage userStorage;
    UserService userService;
    UserController userController;
    User testUser;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
        testUser = new User(0, "", "", "", LocalDate.of(2000, 1, 1));
        testUser.setId(1);
        testUser.setEmail("name@email.com");
        testUser.setLogin("login");
        testUser.setName("Name");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void testValidUser() {
        assertDoesNotThrow(() -> userController.createUser(testUser));
    }

    @Test
    void testNoEmail() {
        testUser.setEmail("");
        assertThrows(UserValidationException.class, () -> userController.createUser(testUser));
    }

    @Test
    void testEmailWithoutAt() {
        testUser.setEmail("name");
        assertThrows(UserValidationException.class, () -> userController.createUser(testUser));
    }

    @Test
    void testNoLogin() {
        testUser.setLogin("");
        assertThrows(UserValidationException.class, () -> userController.createUser(testUser));
    }

    @Test
    void testLoginWithSpace() {
        testUser.setLogin("log in");
        assertThrows(UserValidationException.class, () -> userController.createUser(testUser));
    }

    @Test
    void testNoName() {
        testUser.setName("");
        User createdUser = userController.createUser(testUser);
        assertEquals(createdUser.getName(), createdUser.getLogin());
    }

    @Test
    void testFakeBirthday() {
        System.out.println(testUser.getLogin());
        testUser.setBirthday(LocalDate.of(3000, 1, 1));
        assertThrows(UserValidationException.class, () -> userController.createUser(testUser));
    }
}
