package ru.yandex.practicum.filmorate.storage.user.utils;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class UserValidations {
    public static void prepareCreation(final User user) {
        final String login = user.getLogin();
        if (user.getName() == null || user.getName().trim().isBlank()) {
            log.warn("Получено пустое значение name при добавлении пользователя, поэтому name = login: {}", login);
            user.setName(login);
        }
    }

    public static void prepareUpdate(final Long id, final User user, final User oldUser) {
        if (id == null) {
            log.error("Получен пустой идентификатор пользователя при обновлении");
            throw new ValidationException("Идентификтор пользователя не может быть пустым для команды обновления");
        }

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

    public static void validateOneUserNotNull(User user, Long id) {
        if (user == null) {
            log.error("Получен идентификатор пользователя, который отсутствует в хранилище: {}", id);
            throw new NotFoundException("Идентификтор пользователя неизвестен");
        }
    }

    public static void validateTwoUsersNotNull(User user, User otherUser, Long id, Long otherId) {
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
