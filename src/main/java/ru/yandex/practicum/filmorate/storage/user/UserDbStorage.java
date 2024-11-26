package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.utils.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.utils.UserValidations;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users ORDER BY user_id";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";

    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";

    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";

    private static final String FIND_FRIENDS_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT friend_id FROM user_friends WHERE user_id = ?)";

    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT friend_id FROM user_friends WHERE user_id = ? AND status <> 0 INTERSECT " +
            "SELECT friend_id FROM user_friends WHERE user_id = ?)";

    private static final String DELETE_FRIEND_QUERY = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY,  mapper);
    }

    @Override
    public User create(User user) {
        // проверяем выполнение необходимых условий
        UserValidations.prepareCreation(user);

        // добавляем пользователя в таблицу с формирванием ID из последовательности users.user_id
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        long id = simpleJdbcInsert.executeAndReturnKey(toUserMap(user)).longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        final Long id = user.getId();

        // проверяем необходимые условия
        User oldUser = findOneById(id);
        UserValidations.prepareUpdate(id, user, oldUser);

        // если пользователь найден и все условия соблюдены, обновляем его и возвращаем обновленный объект user
        final int rowsCount = jdbc.update(UPDATE_QUERY, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), id);
        if (rowsCount == 0) {
            log.error("Не найден пользователь с ID = {} при update в БД", id);
            throw new NotFoundException("Не удалось обновить пользователя в БД.");
        }
        return user;
    }

    @Override
    public User delete(Long id) {
        final User oldUser = findOneById(id);
        UserValidations.validateOneUserNotNull(oldUser, id);
        return jdbc.update(DELETE_USER_QUERY, id) > 0 ? oldUser : null;
    }

    @Override
    public Collection<User> findAllFriends(Long id) {
        final User user = findOneById(id);
        UserValidations.validateOneUserNotNull(user, id);
        return jdbc.query(FIND_FRIENDS_QUERY,  mapper, id);
    }

    @Override
    public Collection<User> findCommonFriends(Long id, Long otherId) {
        final User user = findOneById(id);
        final User otherUser = findOneById(otherId);
        UserValidations.validateTwoUsersNotNull(user, otherUser, id, otherId);
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY,  mapper, id, otherId);
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        final User user = findOneById(id);
        final User friendUser = findOneById(friendId);
        UserValidations.validateTwoUsersNotNull(user, friendUser, id, friendId);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("user_friends");
        simpleJdbcInsert.execute(toUserFriendMap(id, friendId));
        // дружба должна стать односторонней: если какой-то пользователь оставил вам заявку в друзья,
        // то он будет в списке ваших друзей, а вы в его — нет: надо удалить "себя" у "друга"
        jdbc.update(DELETE_FRIEND_QUERY, friendId, id);
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        final User user = findOneById(id);
        final User friendUser = findOneById(friendId);
        UserValidations.validateTwoUsersNotNull(user, friendUser, id, friendId);
        return jdbc.update(DELETE_FRIEND_QUERY, id, friendId) > 0 ? friendUser : null;
    }

    @Override
    public boolean exists(Long id) {
        final User user = findOneById(id);
        UserValidations.validateOneUserNotNull(user, id);
        return true;
    }

    private Map<String, Object> toUserMap(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }

    private Map<String, Object> toUserFriendMap(Long userId, Long friendId) {
        Map<String, Object> values = new HashMap<>();
        values.put("user_id", userId);
        values.put("friend_id", friendId);
        values.put("status", 0);    // не подтвержденный статус дружбы при добавлении
        return values;
    }

    private User findOneById(long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }
}
