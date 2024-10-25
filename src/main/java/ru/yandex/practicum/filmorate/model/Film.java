package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;
    private @NotNull @NotBlank String name;
    private @Size(max = 200) String description;
    private LocalDate releaseDate;
    private @Positive int duration;
    private Set<Long> likeUserIds = new HashSet<>();
}
