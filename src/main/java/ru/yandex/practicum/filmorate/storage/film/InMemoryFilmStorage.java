package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.utils.FilmValidations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film findOneById(Long id) {
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        // проверяем выполнение необходимых условий
        FilmValidations.prepareCreation(film);

        // формируем ID
        film.setId(getNextId());

        // сохраняем новый фильм в памяти приложения и возвращаем его
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        final Long id = film.getId();
        // проверяем необходимые условия
        FilmValidations.prepareUpdate(id, film, films.get(id));

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
        final Film film = films.get(id);
        FilmValidations.validateFilmNotNull(film, id);
        film.getLikeUserIds().add(userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        final Film film = films.get(id);
        FilmValidations.validateFilmNotNull(film, id);
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
}
