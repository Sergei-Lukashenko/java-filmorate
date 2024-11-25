package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findOne(Long id) {
        return filmStorage.findOneById(id);
    }

    public Film create(Film film) {
        List<Genre> genres = film.getGenres();
        if (genres == null ||
                (genres != null &&  // если переданы ID жанров, то они должны быть в таблице genres
                genres.stream().allMatch(g -> genreStorage.exists(g.getId()))
                )
        ) {
            return filmStorage.create(film);
        }
        return null;
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
