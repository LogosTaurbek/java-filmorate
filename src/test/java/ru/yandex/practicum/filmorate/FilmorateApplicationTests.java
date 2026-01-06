package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})  //
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userDbStorage;

    @Test
    public void testFindUserById() {
        // Создаём пользователя
        User testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("login");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userDbStorage.createUser(testUser);

        // Проверяем, что ID присвоен
        assertThat(createdUser.getId()).isNotNull();

        // Получаем пользователя из БД
        Optional<User> userOptional = userDbStorage.getUserById(createdUser.getId());

            // Assert
            assertThat(userOptional)
                    .isPresent()
                    .hasValueSatisfying(foundUser -> {
                        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
                        assertThat(foundUser.getEmail()).isEqualTo("test@test.com");
                        assertThat(foundUser.getLogin()).isEqualTo("login");
                        assertThat(foundUser.getName()).isEqualTo("Test User");
                    });
        }
}