package ru.yandex.practicum.filmorate.storage.film.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.film.MpaService;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;


@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Autowired
    private MpaService mpaService;

    @Autowired
    private GenreStorage genreStorage;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        final Long filmId = resultSet.getLong("film_id");
        film.setId(filmId);
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));

        Timestamp releaseDate = resultSet.getTimestamp("release_date");
        film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());

        film.setDuration(resultSet.getInt("duration"));

        Integer mpaId = resultSet.getInt("mpa_rating");
        if (mpaId > 0) {
            film.setMpa(mpaService.findById(mpaId));
        }

        List<Genre> genres = genreStorage.getFilmGenres(filmId);
        film.setGenres(genres);

        return film;
    }
}
