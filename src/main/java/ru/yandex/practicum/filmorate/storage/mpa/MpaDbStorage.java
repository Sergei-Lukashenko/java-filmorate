package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_dict ORDER BY mpa_id";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_dict WHERE mpa_id = ?";

    @Override
    public Collection<Mpa> findAll() {
        return jdbc.query(FIND_ALL_QUERY,  mapper);
    }

    @Override
    public Mpa findOneById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        }  catch (EmptyResultDataAccessException exception) {
            log.error("При запросе MPA-рейтинга получен идентификатор, отсутствующий в таблице mpa_dict: {}", id);
            throw new NotFoundException("Идентификтор MPA-рейтинга не найден", exception);
        }
    }
}
