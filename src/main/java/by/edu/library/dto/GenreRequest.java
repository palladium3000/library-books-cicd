package by.edu.library.dto;

import by.edu.library.model.Genre;
import jakarta.validation.constraints.NotNull;

public record GenreRequest(@NotNull(message = "Нужно указать жанр") Genre genre) {
}
