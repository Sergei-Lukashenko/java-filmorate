package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.utils.UserValidations;

import java.util.*;

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
        UserValidations.prepareCreation(user);

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
        UserValidations.prepareUpdate(id, user, users.get(id));

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
        UserValidations.validateOneUserNotNull(user, id);
        return user.getFriendIds()
                .stream()
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> findCommonFriends(Long id, Long otherId) {
        final User user = users.get(id);
        final User otherUser = users.get(otherId);
        UserValidations.validateTwoUsersNotNull(user, otherUser, id, otherId);
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
        UserValidations.validateTwoUsersNotNull(user, friendUser, id, friendId);
        user.getFriendIds().add(friendId);
        friendUser.getFriendIds().add(id);
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        final User user = users.get(id);
        final User friendUser = users.get(friendId);
        UserValidations.validateTwoUsersNotNull(user, friendUser, id, friendId);
        user.getFriendIds().remove(friendId);
        friendUser.getFriendIds().remove(id);
        return friendUser;
    }

    @Override
    public boolean exists(Long id) {
        final User user = users.get(id);
        UserValidations.validateOneUserNotNull(user, id);
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
}
