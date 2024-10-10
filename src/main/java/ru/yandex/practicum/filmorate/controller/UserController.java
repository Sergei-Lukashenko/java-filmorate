package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Началось добавление пользователя методом POST");

        // проверяем выполнение необходимых условий
        prepareCreation(user);

        // формируем ID
        user.setId(getNextId());

        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Закончилось добавление пользователя {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Началось обновлени пользователя методом PUT");

        // проверяем необходимые условия
        Long id = user.getId();
        prepareUpdate(id, user);

        // если пользователь найден и все условия соблюдены, обновляем его
        users.put(id, user);
        log.info("Закончилось обновление пользователя {}", user);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void prepareCreation(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            log.error("Получено пустое или некорректное значение email = {}", email);
            throw new ValidationException("email должен быть указан при добавлении пользователя");
        }
        String login = user.getLogin();
        if (login == null || login.trim().isBlank()) {
            log.error("Получено пустое значение login");
            throw new ValidationException("login должен быть указан при добавлении пользователя");
        }
        validateBirthday(user.getBirthday());
        if (user.getName() == null || user.getName().trim().isBlank()) {
            log.warn("Получено пустое значение name при добавлении пользователя, поэтому name = login: {}", login);
            user.setName(login);
        }
    }

    private void prepareUpdate(Long id, User user) {
        if (id == null) {
            log.error("Получен пустой идентификатор пользователя при обновлении");
            throw new ValidationException("Идентификтор пользователя не может быть пустым для команды обновления");
        }

        User oldUser = users.get(id);
        if (oldUser == null) {
            log.error("По указанному для обновления ID пользователя {} нет сохраненной информации", id);
            throw new ValidationException("По указанному идентификтору пользователь для обновления не найден");
        }
        if (oldUser.getId() != id) {
            log.error("По указанному идентификатору пользователя {} найден пользователь с другим ID={}", id, oldUser.getId());
            throw new ValidationException("По указанному идентификтору найден пользователь с другим ид.");
        }

        String email = user.getEmail();
        if (email == null || email.trim().isBlank()) {
            log.warn("Не получен или указан пустой email пользователя при обновлении: email не изменяется");
            user.setEmail(oldUser.getEmail());
        } else if (!email.contains("@")) {
            log.error("Получено некорректное значение email = {}", email);
            throw new ValidationException("email должен пользователя должен содержать знак @");
        }

        String login = user.getLogin();
        if (login == null || login.trim().isBlank()) {
            log.warn("Не получен или указан пустой login пользователя при обновлении: login не изменяется");
            user.setLogin(oldUser.getLogin());
        }

        String name = user.getName();
        if (name == null || name.trim().isBlank()) {
            log.warn("Не получено или указано пустое имя пользователя при обновлении: name не изменяется");
            user.setName(oldUser.getName());
        }

        validateBirthday(user.getBirthday());
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            log.error("Получено значение birthday из будущего = {}", birthday);
            throw new ValidationException("birthday не должен быть больше текущей даты");
        }
    }
}
