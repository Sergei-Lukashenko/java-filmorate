package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Методом GET запрошен список фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Началось добавление фильма методом POST");

        // проверяем выполнение необходимых условий
        prepareCreation(film);

        // формируем ID
        film.setId(getNextId());

        // сохраняем новый фильм в памяти приложения
        films.put(film.getId(), film);
        log.info("Закончилось добавление фильма {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Началось обновлени фильма методом PUT");

        // проверяем необходимые условия
        Long id = film.getId();
        prepareUpdate(id, film);

        // если фильм найден и все условия соблюдены, обновляем его
        films.put(id, film);
        log.info("Закончилось обновление фильма {}", film);
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void prepareCreation(final Film film) {
        final String name = film.getName();
        if (name == null || name.trim().isBlank()) {
            log.error("Получено пустое название фильма при добавлении");
            throw new ValidationException("Название добавляемого фильма не может быть пустым");
        }

        final String description = film.getDescription();
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Получено слишком длинное описание фильма при добавлении: {}, более {} символов",
                    description.length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException(String.format(
                    "Максимальная длина описания фильма = 200 символов, а при добавлении фильма получено %d символов",
                    description.length())
            );
        }
        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("При добавлении получена слишком ранняя дата фильма < 28.12.1895");
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            log.error("Получена отрицательная или нулевая продолжительность при добавлении фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private void prepareUpdate(Long id, Film film) {
        if (id == null) {
            log.error("Получен пустой идентификатор фильма при обновлении");
            throw new ValidationException("Идентификтор фильма не может быть пустым для команды обновления");
        }

        Film oldFilm = films.get(id);
        if (oldFilm == null) {
            log.error("По указанному для обновления ID фильма {} нет сохраненной информации", id);
            throw new ValidationException("По указанному идентификтору фильм для обновления не найден");
        }
        if (oldFilm.getId() != id) {
            log.error("По указанному ID фильма для обновления {} найден фильм с другим ID={}", id, oldFilm.getId());
            throw new ValidationException("По указанному идентификтору найден фильм с другим ид.");
        }

        if (film.getName() == null || film.getName().trim().isBlank()) {
            log.warn("Не получено или указано пустое имя фильма при обновлении: name не изменяется");
            film.setName(oldFilm.getName());
        }

        final String description = film.getDescription();
        if (description == null || description.trim().isBlank()) {
            log.warn("Не получено или указано пустое описание фильма при обновлении: description не изменяется");
            film.setDescription(oldFilm.getDescription());
        } else if (description.length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Получено слишком длинное описание фильма при обновлении: {} более {} символов",
                    description.length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException(String.format(
                    "Максимальная длина описания фильма = 200 символов, а при обновлении фильма получено %d символов",
                    description.length()));
        }

        if (film.getReleaseDate() == null) {
            log.warn("Не получена или указана пустая дата выхода фильма при обновлении: releaseDate не изменяется");
            film.setReleaseDate(oldFilm.getReleaseDate());
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("При обновлении получена слишком ранняя дата фильма < 28.12.1895");
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        }

        if (film.getDuration() <= 0) {
            log.warn("Не получена или указана < 0 продолжительность фильма при обновлении: duration не изменяется");
            film.setDuration(oldFilm.getDuration());
        }
    }
}
