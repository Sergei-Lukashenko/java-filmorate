package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    User delete(Long id);

    Collection<User> findAllFriends(Long id);

    Collection<User> findCommonFriends(Long id, Long otherId);

    void addFriend(Long id, Long friendId);

    User deleteFriend(Long id, Long friendId);

    boolean exists(Long id);
}
