package ru.yandex.practicum.filmorate.storage.film.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;


@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final GenreStorage genreStorage;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        final Film film = new Film();
        final Long filmId = resultSet.getLong("film_id");
        film.setId(filmId);
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));

        Timestamp releaseDate = resultSet.getTimestamp("release_date");
        film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());

        film.setDuration(resultSet.getInt("duration"));

        final int mpaId = resultSet.getInt("mpa_id");
        if (mpaId > 0) {
            final String mpaName = resultSet.getString("mpa_name");
            film.setMpa(new Mpa(mpaId, mpaName));
        }

        List<Genre> genres = genreStorage.getFilmGenres(filmId);
        film.setGenres(genres);

        return film;
    }
}
