package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbStorageTests {

    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testGetAllUsers() {
        // Создаём двух пользователей
        User user1 = createTestUser("user1@user.com", "login1", "User One");
        User user2 = createTestUser("user2@user.com", "login2", "User Two");

        // Получаем всех пользователей
        List<User> users = userDbStorage.getAllUsers();

        // Проверяем
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@user.com", "user2@user.com");
        assertThat(users)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("login1", "login2");
    }

    @Test
    public void testCreateUser() {
        // Создаём пользователя
        User testUser = new User();
        testUser.setEmail("user@user.com");
        testUser.setLogin("login");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userDbStorage.createUser(testUser);

        // Проверяем, что ID присвоен
        assertThat(createdUser.getId()).isNotNull();

        // Получаем пользователя из БД
        Optional<User> userOptional = userDbStorage.getUserById(createdUser.getId());

        // Проверяем все поля
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get())
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(createdUser.getId());
                    assertThat(user.getEmail()).isEqualTo("user@user.com");
                    assertThat(user.getLogin()).isEqualTo("login");
                    assertThat(user.getName()).isEqualTo("Test User");
                    assertThat(user.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
                });
    }

    @Test
    public void testGetUserByEmail() {
        // Создаём пользователя
        User createdUser = createTestUser("test@example.com", "testlogin", "Test Name");

        // Ищем по email
        Optional<User> userOptional = userDbStorage.getUserByEmail("test@example.com");

        // Проверяем
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get())
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(createdUser.getId());
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getLogin()).isEqualTo("testlogin");
                    assertThat(user.getName()).isEqualTo("Test Name");
                });
    }

    @Test
    public void testGetUserByLogin() {
        // Создаём пользователя
        User createdUser = createTestUser("user@example.com", "uniquelogin", "User Name");

        // Ищем по login
        Optional<User> userOptional = userDbStorage.getUserByLogin("uniquelogin");

        // Проверяем
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get())
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(createdUser.getId());
                    assertThat(user.getEmail()).isEqualTo("user@example.com");
                    assertThat(user.getLogin()).isEqualTo("uniquelogin");
                    assertThat(user.getName()).isEqualTo("User Name");
                });
    }

    @Test
    public void testUpdateUser() {
        // Создаём пользователя
        User createdUser = createTestUser("original@example.com", "originallogin", "Original Name");

        // Обновляем имя
        createdUser.setName("Updated Name");
        userDbStorage.updateUser(createdUser);

        // Получаем из БД и проверяем
        Optional<User> userOptional = userDbStorage.getUserById(createdUser.getId());

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getName()).isEqualTo("Updated Name");
        assertThat(userOptional.get().getEmail()).isEqualTo("original@example.com");
    }

    @Test
    public void testAddFriend() {
        // Создаём двух пользователей
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");

        // Добавляем в друзья
        userDbStorage.addFriend(user1.getId(), user2.getId());

        // Получаем список друзей
        List<User> friends = userDbStorage.getUserFriends(user1.getId());

        // Проверяем
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
        assertThat(friends.get(0).getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    public void testRemoveFriend() {
        // Создаём двух пользователей
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");

        // Добавляем в друзья
        userDbStorage.addFriend(user1.getId(), user2.getId());
        assertThat(userDbStorage.getUserFriends(user1.getId())).hasSize(1);

        // Удаляем из друзей
        userDbStorage.removeFriend(user1.getId(), user2.getId());

        // Проверяем, что список друзей пуст
        List<User> friends = userDbStorage.getUserFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetUserFriendsWhenNoFriends() {
        // Создаём пользователя без друзей
        User user = createTestUser("lonely@example.com", "lonely", "Lonely User");

        // Получаем список друзей
        List<User> friends = userDbStorage.getUserFriends(user.getId());

        // Проверяем, что список пуст
        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetUserByIdWhenNotExists() {
        // Пытаемся получить несуществующего пользователя
        Optional<User> userOptional = userDbStorage.getUserById(999999);

        // Проверяем, что Optional пустой
        assertThat(userOptional).isEmpty();
    }

    // Helper-метод для создания тестовых пользователей
    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userDbStorage.createUser(user);
    }
}