package by.edu.library.dto;

import by.edu.library.model.BookStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Нужно указать статус") BookStatus status) {
}
