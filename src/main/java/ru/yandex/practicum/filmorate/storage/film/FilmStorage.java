package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findOneById(Long id);

    Film create(Film film);

    Film update(Film film);

    Film delete(Long id);

    Collection<Film> findPopular(Integer count);

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);
}
