package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.utils.GenreRowMapper;

import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";

    private static final String FIND_BY_FILM_ID =
            """
            SELECT g.genre_id, g.name
            FROM film_genres AS fg
            JOIN genres AS g ON g.genre_id = fg.genre_id
            WHERE fg.film_id = ?""";

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY,  mapper);
    }

    @Override
    public Genre findOneById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        }  catch (EmptyResultDataAccessException exception) {
            log.error("При поиске жанра получен идентификатор, отсутствующий в таблице genres: {}", id);
            throw new NotFoundException("Идентификтор жанра не найден", exception);
        }
    }

    @Override
    public boolean exists(Integer id) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return genre != null;
        }  catch (EmptyResultDataAccessException exception) {
            log.error("В ходе проверки жанра получен идентификатор, отсутствующий в таблице genres: {}", id);
            throw new ValidationException("Идентификтор жанра неизвестен", exception);
        }
    }

    @Override
    public List<Genre> getFilmGenres(Long filmId) {
        return jdbc.query(FIND_BY_FILM_ID, mapper, filmId);
    }
}
