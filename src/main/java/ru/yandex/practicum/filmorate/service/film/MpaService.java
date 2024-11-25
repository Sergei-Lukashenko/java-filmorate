package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@Service
public class MpaService {
    // Справочник MPA хранится в памяти, а не в БД, т.к. это фиксированный список, утвержденный профильной организацией:
    // См. https://www.kinopoisk.ru/mpaa
    static Map<Integer, String> mpaIdToName = new HashMap<>();

    static {
        mpaIdToName.put(1, "G");
        mpaIdToName.put(2, "PG");
        mpaIdToName.put(3, "PG-13");
        mpaIdToName.put(4, "R");
        mpaIdToName.put(5, "NC-17");
    }

    public Collection<Mpa> findAll() {
        List<Mpa> mpaDict = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : mpaIdToName.entrySet()) {
            mpaDict.add(new Mpa(entry.getKey(), entry.getValue()));
        }
        return mpaDict;
    }

    public Mpa findById(Integer id) {
        String name = mpaIdToName.get(id);
        if (name == null) {
            log.error("Не найден MPA-рейтинг с ID = {} при обработке GET-запроса", id);
            throw new NotFoundException("Не удалось найти MPA-рейтинг по идентификатору.");
        }
        return new Mpa(id, name);
    }
}
