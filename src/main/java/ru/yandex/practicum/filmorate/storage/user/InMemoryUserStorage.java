package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        // проверяем выполнение необходимых условий
        prepareCreation(user);

        // формируем ID
        user.setId(getNextId());

        // сохраняем нового пользователя в памяти приложения и возвращаем его
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        // проверяем необходимые условия
        prepareUpdate(id, user);

        // если пользователь найден и все условия соблюдены, обновляем его и возвращаем обновленный объект user
        users.put(id, user);
        return user;
    }

    @Override
    public User delete(Long id) {
        return users.remove(id);
    }

    @Override
    public Collection<User> findAllFriends(Long id) {
        final User user = users.get(id);
        validateOneUserNotNull(user, id);
        return user.getFriendIds()
                .stream()
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> findCommonFriends(Long id, Long otherId) {
        final User user = users.get(id);
        final User otherUser = users.get(otherId);
        validateTwoUsersNotNull(user, otherUser, id, otherId);
        Set<Long> commonUserIds = new HashSet<>(user.getFriendIds());
        commonUserIds.retainAll(otherUser.getFriendIds());
        return commonUserIds
                .stream()
                .map(users::get)
                .toList();
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        final User user = users.get(id);
        final User friendUser = users.get(friendId);
        validateTwoUsersNotNull(user, friendUser, id, friendId);
        user.getFriendIds().add(friendId);
        friendUser.getFriendIds().add(id);
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        final User user = users.get(id);
        final User friendUser = users.get(friendId);
        validateTwoUsersNotNull(user, friendUser, id, friendId);
        user.getFriendIds().remove(friendId);
        friendUser.getFriendIds().remove(id);
        return friendUser;
    }

    @Override
    public boolean exists(Long id) {
        final User user = users.get(id);
        validateOneUserNotNull(user, id);
        return true;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void prepareCreation(final User user) {
        final String login = user.getLogin();
        if (user.getName() == null || user.getName().trim().isBlank()) {
            log.warn("Получено пустое значение name при добавлении пользователя, поэтому name = login: {}", login);
            user.setName(login);
        }
    }

    private void prepareUpdate(final Long id, final User user) {
        if (id == null) {
            log.error("Получен пустой идентификатор пользователя при обновлении");
            throw new ValidationException("Идентификтор пользователя не может быть пустым для команды обновления");
        }

        final User oldUser = users.get(id);
        if (oldUser == null) {
            log.error("По указанному для обновления ID пользователя {} нет сохраненной информации", id);
            throw new NotFoundException("По указанному идентификтору пользователь для обновления не найден");
        }
        if (!oldUser.getId().equals(id)) {
            log.error("По указанному идентификатору пользователя {} найден пользователь с другим ID={}",
                    id, oldUser.getId());
            throw new ValidationException("По указанному идентификтору найден пользователь с другим ид.");
        }

        final String login = user.getLogin();
        if (login == null || login.trim().isBlank()) {
            log.warn("Не получен или указан пустой login пользователя при обновлении: login не изменяется");
            user.setLogin(oldUser.getLogin());
        }

        final String name = user.getName();
        if (name == null || name.trim().isBlank()) {
            log.warn("Не получено или указано пустое имя пользователя при обновлении: name не изменяется");
            user.setName(oldUser.getName());
        }
    }

    private void validateOneUserNotNull(User user, Long id) {
        if (user == null) {
            log.error("Получен идентификатор пользователя, который отсутствует в хранилище: {}", id);
            throw new NotFoundException("Идентификтор пользователя неизвестен");
        }
    }

    private void validateTwoUsersNotNull(User user, User otherUser, Long id, Long otherId) {
        if (user == null) {
            log.error("Получен идентификатор 1-го пользователя, который отсутствует в хранилище: {}", id);
            throw new NotFoundException("Идентификтор 1-го пользователя неизвестен");
        }
        if (otherUser == null) {
            log.error("Получен идентификатор 2-го пользователя, который отсутствует в хранилище: {}", otherId);
            throw new NotFoundException("Идентификтор 2-го пользователя неизвестен");
        }
    }
}
