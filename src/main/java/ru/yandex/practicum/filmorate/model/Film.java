package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;
    private @NotNull @NotBlank String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
}
