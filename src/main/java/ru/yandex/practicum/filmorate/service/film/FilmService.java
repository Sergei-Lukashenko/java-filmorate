package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film delete(Long id) {
        return filmStorage.delete(id);
    }

    public Collection<Film> findPopular(Integer count) {
        return filmStorage.findPopular(count);
    }

    public void addLike(Long id, Long userId) {
        if (userStorage.exists(userId)) {
            filmStorage.addLike(id, userId);
        }
    }

    public void deleteLike(Long id, Long userId) {
        if (userStorage.exists(userId)) {
            filmStorage.deleteLike(id, userId);
        }
    }
}
