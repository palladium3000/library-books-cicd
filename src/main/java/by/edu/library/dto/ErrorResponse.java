package by.edu.library.dto;

import java.time.LocalDateTime;

/**
 * JSON, который отдаётся клиенту при ошибке.
 */
public record ErrorResponse(LocalDateTime time, int status, String message) {
}
