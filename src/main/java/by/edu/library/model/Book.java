package by.edu.library.model;

import java.time.LocalDateTime;

/**
 * Книга в каталоге.
 */
public record Book(long id,
                   String title,
                   String author,
                   int year,
                   Genre genre,
                   BookStatus status,
                   LocalDateTime addedAt) {

    // копия книги с другим статусом
    public Book withStatus(BookStatus newStatus) {
        return new Book(id, title, author, year, genre, newStatus, addedAt);
    }

    // копия книги с другим жанром
    public Book withGenre(Genre newGenre) {
        return new Book(id, title, author, year, newGenre, status, addedAt);
    }
}
