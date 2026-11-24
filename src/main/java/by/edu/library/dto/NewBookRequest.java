package by.edu.library.dto;

import by.edu.library.model.Genre;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Данные для добавления книги (POST /api/books).
 * genre можно не указывать - тогда OTHER.
 */
public record NewBookRequest(
        @NotBlank(message = "Название книги обязательно")
        @Size(max = 150, message = "Название слишком длинное")
        String title,
        @NotBlank(message = "Автор обязателен")
        String author,
        @Min(value = 1450, message = "Год издания не раньше 1450")
        @Max(value = 2026, message = "Год издания не позже 2026")
        int year,
        Genre genre) {
}
