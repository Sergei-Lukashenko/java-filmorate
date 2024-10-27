package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */
@Data
public class User {
    private Long id;

    @NotEmpty
    @Email(message = "Некорректный email.")
    private String email;

    @NotBlank(message = "Логин не может быть пустым.")
    @Pattern(regexp = "\\S*", message = "Логин содержит пробелы.")
    // S - любой непробельный символ, * - ноль или более раз
    private String login;

    private String name;

    @NotNull
    @PastOrPresent(message = "Некорректно указана дата рождения.")
    private LocalDate birthday;

    private Set<Long> friendIds = new HashSet<>();
}
