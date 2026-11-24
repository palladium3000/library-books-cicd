package by.edu.library.service;

import by.edu.library.model.Book;
import by.edu.library.model.BookStatus;
import by.edu.library.model.Genre;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Логика каталога. Книги лежат в памяти (ConcurrentHashMap), без базы данных.
 */
@Service
public class BookService {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    public Book add(String title, String author, int year, Genre genre) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название книги обязательно");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Автор обязателен");
        }
        if (year < 1450 || year > 2026) {
            throw new IllegalArgumentException("Неправильный год издания: " + year);
        }
        Genre g = (genre != null) ? genre : Genre.OTHER;
        long id = counter.incrementAndGet();
        Book book = new Book(id, title.trim(), author.trim(), year, g, BookStatus.AVAILABLE, LocalDateTime.now());
        books.put(id, book);
        return book;
    }

    public List<Book> getAll() {
        List<Book> list = new ArrayList<>(books.values());
        list.sort(Comparator.comparingLong(Book::id));
        return list;
    }

    public List<Book> getByGenre(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Жанр не указан");
        }
        List<Book> result = new ArrayList<>();
        for (Book b : getAll()) {
            if (b.genre() == genre) {
                result.add(b);
            }
        }
        return result;
    }

    public Book get(long id) {
        Book b = books.get(id);
        if (b == null) {
            throw new BookNotFoundException(id);
        }
        return b;
    }

    /**
     * Выдача / возврат / списание. Списанную книгу выдать нельзя.
     */
    public Book setStatus(long id, BookStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Нужно указать статус");
        }
        Book old = get(id);
        if (old.status() == BookStatus.WRITTEN_OFF && status == BookStatus.ISSUED) {
            throw new IllegalArgumentException("Списанную книгу нельзя выдать");
        }
        Book updated = old.withStatus(status);
        books.put(id, updated);
        return updated;
    }

    public Book setGenre(long id, Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Нужно указать жанр");
        }
        Book updated = get(id).withGenre(genre);
        books.put(id, updated);
        return updated;
    }

    // сколько книг в каталоге с нужным статусом (например, сколько сейчас выдано)
    public long countByStatus(BookStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Нужно указать статус");
        }
        return books.values().stream().filter(b -> b.status() == status).count();
    }

    public void remove(long id) {
        if (books.remove(id) == null) {
            throw new BookNotFoundException(id);
        }
    }
}
