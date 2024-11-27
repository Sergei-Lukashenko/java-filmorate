package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Genres dictionary.
 */
@Data
public class Genre {
    @NotNull
    private Integer id;
    @NotNull
    private String name;

    @JsonCreator
    public Genre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
