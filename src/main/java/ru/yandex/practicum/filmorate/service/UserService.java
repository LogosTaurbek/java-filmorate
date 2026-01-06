package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.UserValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    public UserStorage userStorage;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.userStorage = userStorage;
    }

    public List<UserDto> getAllUsers() {
        List<UserDto> usrsDto = this.userStorage.getAllUsers().stream()
                .map(user -> {
                    UserDto userDto = UserMapper.mapToUserDto(user);
                    userDto.setFriends(userStorage.getUserFriends(user.getId()));
                    return userDto;
                })
                .collect(Collectors.toList());
        return usrsDto;
    }

    public UserDto createUser(NewUserRequest newUser) {
        NewUserRequest validatedRequest = validateUser(newUser);
        log.info("Создание пользователя с именем = " + validatedRequest.getName());
        Optional<User> userAlreadyExists = this.userStorage.getUserByEmail(validatedRequest.getEmail());
        if (userAlreadyExists.isPresent()) {
            throw new DuplicatedDataException("Данный имейл уже используется: " + validatedRequest.getEmail());
        }
        userAlreadyExists = userStorage.getUserByLogin(validatedRequest.getLogin());
        if (userAlreadyExists.isPresent()) {
            throw new DuplicatedDataException("Данный логин уже используется: " + validatedRequest.getLogin());
        }
        User user = UserMapper.mapToUser(validatedRequest);
        user = userStorage.createUser(user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) throws NoSuchElementException {
        User updatedUser = userStorage.getUserById(request.getId())
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователя с id=" + request.getId() + " нет в системе."
                ));
        updatedUser = userStorage.updateUser(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public UserDto getUserById(int id) {
        log.info("Получение пользователя с id = " + id);
        Optional<User> optUser = userStorage.getUserById(id);
        if (optUser.isEmpty()) {
            throw new NoSuchElementException("Пользователя с ID=" + id + "нет в БД.");
        }
        UserDto usrDto = UserMapper.mapToUserDto((optUser.get()));
        usrDto.setFriends(userStorage.getUserFriends(id));
        return usrDto;
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
    }

    public void removeUser(int userId) {
        log.info("Удаление пользователя с id = " + userId);
        if (!isUserExist(userId)) {
            throw new NoSuchElementException("Пользователя с id=" + userId + " нет в системе.");
        }
        this.userStorage.removeUser(userId);
    }

    public List<UserDto> listCommonFriends(int user1Id, int user2Id) {
        log.info("Получение списка общих друзей пользователя с id = " + user1Id + " и пользователя с id = " + user2Id);
        if (user1Id == user2Id) {
            throw new NoSuchElementException("Пользователи с id=" + user1Id + " одинаковые");
        }
        return userStorage.getCommonFriends(user1Id, user2Id).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }


    public List<UserDto> getUserFriends(int id) {
        log.info("Получение списка друзей пользователя с id = " + id);
        if (!this.isUserExist(id)) {
            throw new NoSuchElementException(
                    "Пользователя с id=" + id + " нет в системе."
            );
        }
        return this.userStorage.getUserFriends(id).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public boolean isUserExist(int userId) {
        log.info("Проверка существования пользователя с id = " + userId);
        Optional<User> optUser = this.userStorage.getUserById(userId);
        return optUser.isPresent();
    }

    // Метод возвращает объект User, потому что в процессе валидации объект может измениться
    public NewUserRequest validateUser(NewUserRequest user) throws UserValidationException {
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
