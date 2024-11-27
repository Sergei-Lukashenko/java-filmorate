package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Методом GET запрошен список фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Методом GET запрошен фильм с ID = " + id);
        return filmService.findOne(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Началось добавление фильма методом POST");
        final Film newFilm = filmService.create(film);
        log.info("Закончилось добавление фильма {}", newFilm);
        return newFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Началось обновление фильма c ID = {} методом PUT", film.getId());
        final Film updatedFilm = filmService.update(film);
        log.info("Закончилось обновление фильма {}", updatedFilm);
        return updatedFilm;
    }

    @DeleteMapping("/{id}")
    public Film delete(@PathVariable Long id) {
        log.info("Поступил запрос на удаление фильма с ID = {}", id);
        final Film deletedFilm = filmService.delete(id);
        log.info("Закончилось удаление фильма {}", deletedFilm);
        return deletedFilm;
    }

    @GetMapping("/popular")
    public Collection<Film> findPopular(@RequestParam(defaultValue = "10") @Positive Integer count) {
        log.info("Методом GET запрошен список популярных фильмов в количестве {}", count);
        return filmService.findPopular(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        log.info("Началось обновление фильма c ID = {} методом PUT для добавления лайка от пользователя c ID = {}",
                id, userId);
        filmService.addLike(id, userId);
        log.info("Закончилось добавление фильму ID = {} лайка от пользователя ID = {}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    // --> userId whom like was removed for the film
    public Long deleteLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        log.info("Началось удаление лайка от пользователя ID = {} у фильма c ID = {} методом DELETE",
                userId, id);
        filmService.deleteLike(id, userId);
        log.info("Закончилось удаление лайка от пользователя ID = {} у фильма c ID = {}", userId, id);
        return userId;
    }
}
