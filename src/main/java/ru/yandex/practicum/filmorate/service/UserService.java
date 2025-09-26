package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    public UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return this.userStorage.getAllUsers();
    }

    public User createUser(User newUser) {
        validateUser(newUser);
        log.info("Создание пользователя с именем = " + newUser.getName());
        return this.userStorage.createUser(newUser);
    }

    public User updateUser(User updatedUser) throws NoSuchElementException {
        log.info("Редактирование пользователя с id = " + updatedUser.getId());
        if (!this.isUserExist(updatedUser.getId())) {
            throw new NoSuchElementException("Пользователя с id=" + updatedUser.getId() + " нет в системе.");
        } else {
            validateUser(updatedUser);
            return this.userStorage.updateUser(updatedUser);
        }
    }

    public User getUserById(int id) {
        log.info("Получение пользователя с id = " + id);
        return this.getUserByIdWithException(id);
    }

    public void addFriend(int user1Id, int user2Id) {
        // Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
        // То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.
        log.info("Добавление в друзья пользователю с id = " + user1Id + " пользователя с id + " + user2Id);
        if (!isUserExist(user1Id)) {
            throw new NoSuchElementException("Пользователя с id=" + user1Id + " нет в системе.");
        } else if (!isUserExist(user2Id)) {
            throw new NoSuchElementException("Пользователя с id=" + user2Id + " нет в системе.");
        } else if (user1Id == user2Id) {
            throw new NoSuchElementException("Пользователи с id=" + user1Id + " одинаковые");
        } else {
            this.userStorage.addFriend(user1Id, user2Id);
            this.userStorage.addFriend(user2Id, user1Id);
        }
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Удаление с друзей пользователя с id = " + userId + " пользователя с id + " + friendId);
        if (!isUserExist(userId)) {
            throw new NoSuchElementException("Пользователя с id=" + userId + " нет в системе.");
        }
        if (!isUserExist(friendId)) {
            throw new NoSuchElementException("Пользователя с id=" + friendId + " нет в системе.");
        }
        if (userId == friendId) {
            throw new NoSuchElementException("Пользователи с id=" + userId + " одинаковые");
        }
        this.userStorage.removeFriend(userId, friendId);
        this.userStorage.removeFriend(friendId, userId);
    }

    public void removeUser(int userId) {
        log.info("Удаление пользователя с id = " + userId);
        if (!isUserExist(userId)) {
            throw new NoSuchElementException("Пользователя с id=" + userId + " нет в системе.");
        }
        this.userStorage.removeUser(userId);
    }

    public List<User> listCommonFriends(int user1Id, int user2Id) {
        log.info("Получение списка общих друзей пользователя с id = " + user1Id + " и пользователя с id = " + user2Id);
        if (user1Id == user2Id) {
            throw new NoSuchElementException("Пользователи с id=" + user1Id + " одинаковые");
        }
        Set<Integer> friendsOfUser1 = this.getUserByIdWithException(user1Id).getFriendIds();
        Set<Integer> friendsOfUser2 = this.getUserByIdWithException(user2Id).getFriendIds();
        friendsOfUser1.retainAll(friendsOfUser2);
        return friendsOfUser1
                .stream()
                .map(userId -> this.getUserByIdWithException(userId))
                .toList();
    }

    public List<User> getUserFriends(int id) {
        log.info("Получение списка друзей пользователя с id = " + id);
        return this.getUserByIdWithException(id)
                .getFriendIds()
                .stream()
                .map(userId -> this.getUserByIdWithException(userId))
                .toList();
    }

    public boolean isUserExist(int userId) {
        log.info("Проверка существования пользователя с id = " + userId);
        return this.userStorage.getUserById(userId) != null;
    }

    // Метод возвращает объект User, потому что в процессе валидации объект может измениться
    public User validateUser(User user) throws UserValidationException {
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
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Указана нереалистичная дата рождения. {}", user);
            throw new UserValidationException("Дата рождения не может быть в будущем.");
        }
        log.info("Валидация пользователя с именем = " + user.getName());
        return user;
    }

    private User getUserByIdWithException(int id) {
        log.info("Получение пользователя с id = " + id);
        Optional<User> optUser = this.userStorage.getUserById(id);
        if (optUser.isEmpty()) {
            throw new NoSuchElementException("Пользователя с id=" + id + " нет в системе.");
        } else return optUser.get();
    }
}
