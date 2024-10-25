package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Методом GET запрошен список пользователей");
        return userService.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Началось добавление пользователя методом POST");
        final User newUser = userService.create(user);
        log.info("Закончилось добавление пользователя {}", newUser);
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Началось обновление пользователя c ID = {} методом PUT", user.getId());
        final User updatedUser = userService.update(user);
        log.info("Закончилось обновление пользователя {}", updatedUser);
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public User delete(@PathVariable Long id) {
        log.info("Поступил запрос на удаление пользователя с ID = {} методом DELETE", id);
        final User deletedUser = userService.delete(id);
        log.info("Закончилось удаление пользователя {}", deletedUser);
        return deletedUser;
    }

    @GetMapping("/{id}/friends")
    public Collection<User> findAllFriends(@PathVariable Long id) {
        log.info("Методом GET запрошен список всех друзей пользователя с ID = {}", id);
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriends(@PathVariable Long id,
                                              @PathVariable Long otherId) {
        log.info("Методом GET запрошен список общих друзей пользователей с ID = {} и {}",
                id, otherId);
        return userService.findCommonFriends(id, otherId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id,
                          @PathVariable Long friendId) {
        log.info("Началось обновление пользователя c ID = {} методом PUT для добавления ему друга c ID = {}",
                id, friendId);
        userService.addFriend(id, friendId);
        log.info("Закончилось добавление пользователю ID = {} друга ID = {}", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        log.info("Началось удаление друга ID = {} у пользователя c ID = {} методом DELETE",
                 friendId, id);
        userService.deleteFriend(id, friendId);
        log.info("Закончилось удаление друга ID = {} у пользователя c ID = {}", friendId, id);
    }
}
