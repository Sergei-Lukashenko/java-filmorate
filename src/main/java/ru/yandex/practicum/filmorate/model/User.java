package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

/**
 * User.
 */
@Data
public class User {
    private Long id;
    private @Email String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
