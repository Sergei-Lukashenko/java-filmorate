package ru.yandex.practicum.filmorate.storage.film.utils;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class FilmValidations {
    private static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public static void prepareCreation(final Film film) {
        final LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate != null && releaseDate.isBefore(FILM_BIRTHDAY)) {
            log.error("При добавлении получена слишком ранняя дата фильма {} < 28.12.1895",
                    releaseDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        }
        validateMpa(film.getMpa());
    }

    public static void prepareUpdate(final Long id, final Film film, final Film oldFilm) {
        if (id == null) {
            log.error("Получен пустой идентификатор фильма при обновлении");
            throw new ValidationException("Идентификтор фильма не может быть пустым для команды обновления");
        }

        if (oldFilm == null) {
            log.error("По указанному для обновления ID фильма {} нет сохраненной информации", id);
            throw new NotFoundException("По указанному идентификтору фильм для обновления не найден");
        }
        if (!oldFilm.getId().equals(id)) {
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
        }

        final LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate == null) {
            log.warn("Не получена или указана пустая дата выхода фильма при обновлении: releaseDate не изменяется");
            film.setReleaseDate(oldFilm.getReleaseDate());
        } else if (releaseDate.isBefore(FILM_BIRTHDAY)) {
            log.error("При обновлении получена слишком ранняя дата фильма {} < 28.12.1895", releaseDate);
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        }

        validateMpa(film.getMpa());
    }

    public static void validateFilmNotNull(Film film, Long id) {
        if (film == null) {
            log.error("По указанному ID фильма {} нет сохраненной информации", id);
            throw new NotFoundException("По указанному идентификтору фильм не найден");
        }
    }

    private static void validateMpa(final Mpa mpa) {
        if (mpa == null) {
            return;
        }
        final int mpaId = mpa.getId();
        if (!(mpaId >= 1 && mpaId <= 5)) {
            log.error("При добавлении/обновлении фильма получено некорректное значение MPA-рейинга = {}", mpaId);
            throw new ValidationException("Недопустимое значение MPA-рейтинга");
        }
    }
}
