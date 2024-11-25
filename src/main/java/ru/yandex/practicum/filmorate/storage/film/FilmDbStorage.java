package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.utils.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.utils.FilmValidations;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    private final GenreDbStorage genreDbStorage;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films ORDER BY film_id";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";

    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_rating = ? WHERE film_id = ?";

    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    private static final String FIND_POPULAR =
            """
            SELECT * FROM films AS f
            JOIN
                (SELECT film_id,
                 count(user_id) AS likes_count
                FROM film_likes
                GROUP BY film_id
                ORDER BY likes_count DESC
                LIMIT (?)) AS t ON f.film_id = t.film_id
            ORDER BY t.likes_count DESC""";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY,  mapper);
        for (Film f : films) {
            List<Genre> genres = genreDbStorage.getFilmGenres(f.getId());
            f.setGenres(genres);
        }
        return films;
    }

    @Override
    public Film findOneById(Long id) {
        try {
            Film film = (Film) jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return film;
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public Film create(Film film) {
        // проверяем выполнение необходимых условий
        FilmValidations.prepareCreation(film);

        // добавляем фильм в таблицу с формирванием ID из последовательности films.film_id
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        long id = simpleJdbcInsert.executeAndReturnKey(toFilmMap(film)).longValue();
        film.setId(id);

        // добавляем жанры фильма в таблицу film_genres, если они есть
        List<Genre> genres = film.getGenres();
        if (genres == null) {
            return film;
        }

        for (Genre genre : genres) {
            simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("film_genres");
            try {
                simpleJdbcInsert.execute(toFilmGenresMap(id, genre.getId()));
            }  catch (DuplicateKeyException exception) {
                log.warn("Комбинация film_id = {} и genre_id = {} уже есть в БД!", id, genre.getId());
                //throw new ValidationException("Идентификтор фильма не может быть пустым для команды обновления", exception);
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        final Long id = film.getId();

        // проверяем необходимые условия
        Film oldFilm = findOneById(id);
        FilmValidations.prepareUpdate(id, film, oldFilm);

        // если фильм найден и все условия соблюдены, обновляем его и возвращаем обновленный объект film
        final Mpa mpa = film.getMpa();
        final int rowsCount = jdbc.update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), (mpa == null ? null: mpa.getId()), id);
        if (rowsCount == 0) {
            log.error("Не найден фильм с ID = {} при update в БД", id);
            throw new NotFoundException("Не удалось обновить фильм в БД.");
        }
        return film;
    }

    @Override
    public Film delete(Long id) {
        final Film oldFilm = findOneById(id);
        FilmValidations.validateFilmNotNull(oldFilm, id);
        return jdbc.update(DELETE_FILM_QUERY, id) > 0 ? oldFilm : null;
    }

    @Override
    public Collection<Film> findPopular(Integer count) {
        return jdbc.query(FIND_POPULAR,  mapper, count);
    }

    @Override
    public void addLike(Long id, Long userId) {
        final Film film = findOneById(id);
        FilmValidations.validateFilmNotNull(film, id);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("film_likes");
        simpleJdbcInsert.execute(toFilmLikesdMap(id, userId));
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        final Film film = findOneById(id);
        FilmValidations.validateFilmNotNull(film, id);
        jdbc.update(DELETE_LIKE_QUERY, id, userId);
    }

    private Map<String, Object> toFilmMap(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        Mpa mpa = film.getMpa();
        if (mpa != null) {
            values.put("mpa_rating", film.getMpa().getId());
        }
        return values;
    }

    private Map<String, Object> toFilmLikesdMap(Long filmId, Long userId) {
        Map<String, Object> values = new HashMap<>();
        values.put("film_id", filmId);
        values.put("user_id", userId);
        return values;
    }

    private Map<String, Object> toFilmGenresMap(Long filmId, Integer genreId) {
        Map<String, Object> values = new HashMap<>();
        values.put("film_id", filmId);
        values.put("genre_id", genreId);
        return values;
    }
}
