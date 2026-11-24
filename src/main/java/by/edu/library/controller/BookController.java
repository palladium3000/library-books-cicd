package by.edu.library.controller;

import by.edu.library.dto.GenreRequest;
import by.edu.library.dto.NewBookRequest;
import by.edu.library.dto.StatusRequest;
import by.edu.library.model.Book;
import by.edu.library.model.Genre;
import by.edu.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API каталога книг.
 *   GET    /api/books            - все книги (можно ?genre=FICTION)
 *   GET    /api/books/{id}       - одна книга
 *   POST   /api/books            - добавить
 *   PUT    /api/books/{id}/status - выдать / вернуть / списать
 *   PUT    /api/books/{id}/genre  - поменять жанр
 *   DELETE /api/books/{id}       - удалить из каталога
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> list(@RequestParam(name = "genre", required = false) Genre genre) {
        return genre == null ? bookService.getAll() : bookService.getByGenre(genre);
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable long id) {
        return bookService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book add(@Valid @RequestBody NewBookRequest req) {
        return bookService.add(req.title(), req.author(), req.year(), req.genre());
    }

    @PutMapping("/{id}/status")
    public Book status(@PathVariable long id, @Valid @RequestBody StatusRequest req) {
        return bookService.setStatus(id, req.status());
    }

    @PutMapping("/{id}/genre")
    public Book genre(@PathVariable long id, @Valid @RequestBody GenreRequest req) {
        return bookService.setGenre(id, req.genre());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        bookService.remove(id);
    }
}
