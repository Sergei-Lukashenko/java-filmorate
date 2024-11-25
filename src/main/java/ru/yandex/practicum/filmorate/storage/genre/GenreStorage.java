package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreStorage {

    Collection<Genre> findAll();

    Genre findOneById(Integer id);

    boolean exists(Integer id);

    List<Genre> getFilmGenres(Long filmId);
}