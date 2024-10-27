package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    private static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        // проверяем выполнение необходимых условий
        prepareCreation(film);

        // формируем ID
        film.setId(getNextId());

        // сохраняем новый фильм в памяти приложения и возвращаем его
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        // проверяем необходимые условия
        prepareUpdate(id, film);

        // если фильм найден и все условия соблюдены, обновляем его и возвращаем обновленный объект film
        films.put(id, film);
        return film;
    }

    @Override
    public Film delete(Long id) {
        return films.remove(id);
    }

    @Override
    public Collection<Film> findPopular(Integer count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int comp = Integer.compare(f1.getLikeUserIds().size(), f2.getLikeUserIds().size());
                    return -comp;  // descending
                }).limit(count).toList();
    }

    @Override
    public void addLike(Long id, Long userId) {
        Film film = films.get(id);
        validateFilmNotNull(film, id);
        film.getLikeUserIds().add(userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        final Film film = films.get(id);
        validateFilmNotNull(film, id);
        film.getLikeUserIds().remove(userId);
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
        final LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate != null && releaseDate.isBefore(FILM_BIRTHDAY)) {
            log.error("При добавлении получена слишком ранняя дата фильма {} < 28.12.1895",
                    releaseDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        }
    }

    private void prepareUpdate(final Long id, final Film film) {
        if (id == null) {
            log.error("Получен пустой идентификатор фильма при обновлении");
            throw new ValidationException("Идентификтор фильма не может быть пустым для команды обновления");
        }

        final Film oldFilm = films.get(id);
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
        } else if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("При обновлении получена слишком ранняя дата фильма {} < 28.12.1895", releaseDate);
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        }
    }

    private void validateFilmNotNull(Film film, Long id) {
        if (film == null) {
            log.error("По указанному ID фильма {} нет сохраненной информации", id);
            throw new NotFoundException("По указанному идентификтору фильм не найден");
        }
    }
}
