package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * MPAs dictionary.
 */
@Data
public class Mpa {
    @NotNull
    private Integer id;
    @NotNull
    private String name;

    @JsonCreator
    public Mpa(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
