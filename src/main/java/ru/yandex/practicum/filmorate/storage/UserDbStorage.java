package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseBdStorage<User> implements UserStorage {
    private static final String GET_ALL_QUERY = "SELECT * FROM users";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String GET_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String GET_BY_LOGIN_QUERY = "SELECT * FROM users WHERE login = ?";
    private static final String UPDATE_QUERY =
            "UPDATE users " +
                    "SET email = ?, login = ?, name = ?, birthday = ? " +
                    "WHERE id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO users(email, login, name, birthday)" +
                    "VALUES (?, ?, ?, ?)";
    private static final String ADD_FRIEND_QUERY =
            "INSERT into friendships(friendFrom, friendTo, acceptanceStatus) " +
                    "VALUES (?, ?, ?)";
    private static final String GET_USER_FRIENDS_QUERY =
            "SELECT friendTo FROM friendships " +
                    "WHERE friendFrom = ?";
    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendships " +
                    "WHERE friendFrom = ? " +
                    "AND friendTo = ?";
    private static final String REMOVE_USER_QUERY =
            "DELETE FROM users " +
                    "WHERE id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> getAllUsers() {
        return findMany(GET_ALL_QUERY).stream()
                .peek(user -> user.setFriends(getUserFriends(user.getId())))
                .toList();
    }

    @Override
    public Optional<User> getUserById(int id) {
        Optional<User> optUser = findOne(GET_BY_ID_QUERY, id);
        if (optUser.isEmpty()) {
            return optUser;
        }
        User user = optUser.get();
        user.setFriends(this.getUserFriends(user.getId()));
        return Optional.of(user);
    }

    public Optional<User> getUserByIdWithFriends(int id) {
        Optional<User> optUser = findOne(GET_BY_ID_QUERY, id);
        if (optUser.isEmpty()) {
            return optUser;
        }
        User user = optUser.get();
        user.setFriends(this.getUserFriends(user.getId()));
        return Optional.of(user);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return findOne(GET_BY_EMAIL_QUERY, email);
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return findOne(GET_BY_LOGIN_QUERY, login);
    }

    @Override
    public User createUser(User newUser) {
        int id = insert(
                INSERT_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday()
        );
        newUser.setId(id);
        return newUser;
    }

    @Override
    public User updateUser(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void removeUser(int userId) {
        jdbc.update(REMOVE_USER_QUERY, userId);
    }

    @Override
    public void addFriend(int user1Id, int user2Id) {
        insertWithoutGeneratedId(ADD_FRIEND_QUERY, user1Id, user2Id, FriendshipStatus.UNCONFIRMED.toString());
    }

    @Override
    public List<User> getUserFriends(int userId) {
        List<Integer> friendIds = jdbc.queryForList(GET_USER_FRIENDS_QUERY, Integer.class, userId);
        return friendIds.stream()
                .map(id -> getUserById(id).get())
                .toList();
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        int rowsDeleted = jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }
}
