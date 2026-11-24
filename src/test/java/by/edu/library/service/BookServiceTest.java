package by.edu.library.service;

import by.edu.library.model.Book;
import by.edu.library.model.BookStatus;
import by.edu.library.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты BookService (без Spring).
 */
class BookServiceTest {

    BookService service;

    @BeforeEach
    void init() {
        service = new BookService();
    }

    @Test
    @DisplayName("Добавленная книга получает номер 1 и статус AVAILABLE")
    void addFirstBook() {
        Book b = service.add(" Мастер и Маргарита ", " Булгаков ", 1967, Genre.FICTION);
        assertAll(
                () -> assertEquals(1, b.id()),
                () -> assertEquals("Мастер и Маргарита", b.title()),
                () -> assertEquals("Булгаков", b.author()),
                () -> assertEquals(BookStatus.AVAILABLE, b.status()),
                () -> assertNotNull(b.addedAt()));
    }

    @Test
    void genreByDefaultIsOther() {
        assertEquals(Genre.OTHER, service.add("Справочник", "Авторский коллектив", 2001, null).genre());
    }

    @Test
    void emptyTitleOrAuthorNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> service.add("", "Пушкин", 1833, Genre.FICTION));
        assertThrows(IllegalArgumentException.class, () -> service.add("Евгений Онегин", "  ", 1833, Genre.FICTION));
    }

    @Test
    void yearBoundaries() {
        assertEquals(1450, service.add("Старая книга", "Неизвестен", 1450, null).year());
        assertEquals(2026, service.add("Новая книга", "Неизвестен", 2026, null).year());
        assertThrows(IllegalArgumentException.class, () -> service.add("Слишком старая", "Неизвестен", 1449, null));
        assertThrows(IllegalArgumentException.class, () -> service.add("Из будущего", "Неизвестен", 2027, null));
    }

    @Test
    void getAllInOrderOfAdding() {
        service.add("Первая", "А", 2000, null);
        service.add("Вторая", "Б", 2001, null);
        List<Book> all = service.getAll();
        assertEquals(2, all.size());
        assertEquals("Первая", all.get(0).title());
        assertEquals("Вторая", all.get(1).title());
    }

    @Test
    void filterByGenre() {
        service.add("Война и мир", "Толстой", 1869, Genre.FICTION);
        service.add("Краткая история времени", "Хокинг", 1988, Genre.SCIENCE);
        service.add("Анна Каренина", "Толстой", 1878, Genre.FICTION);
        List<Book> fiction = service.getByGenre(Genre.FICTION);
        assertEquals(2, fiction.size());
        assertTrue(fiction.stream().allMatch(b -> b.genre() == Genre.FICTION));
        assertTrue(service.getByGenre(Genre.CHILDREN).isEmpty());
    }

    @Test
    void filterWithoutGenreIsError() {
        assertThrows(IllegalArgumentException.class, () -> service.getByGenre(null));
    }

    @Test
    void bookNotFound() {
        assertThrows(BookNotFoundException.class, () -> service.get(42));
    }

    @Test
    void issueAndReturnBook() {
        Book b = service.add("Дубровский", "Пушкин", 1841, Genre.FICTION);
        assertEquals(BookStatus.ISSUED, service.setStatus(b.id(), BookStatus.ISSUED).status());
        Book back = service.setStatus(b.id(), BookStatus.AVAILABLE);
        assertEquals(BookStatus.AVAILABLE, back.status());
        assertEquals(Genre.FICTION, back.genre());
    }

    @Test
    void writtenOffBookCannotBeIssued() {
        Book b = service.add("Старый учебник", "Иванов", 1980, Genre.SCIENCE);
        service.setStatus(b.id(), BookStatus.WRITTEN_OFF);
        assertThrows(IllegalArgumentException.class, () -> service.setStatus(b.id(), BookStatus.ISSUED));
    }

    @Test
    void changeGenreKeepsStatus() {
        Book b = service.add("Маленький принц", "Экзюпери", 1943, Genre.FICTION);
        service.setStatus(b.id(), BookStatus.ISSUED);
        Book upd = service.setGenre(b.id(), Genre.CHILDREN);
        assertEquals(Genre.CHILDREN, upd.genre());
        assertEquals(BookStatus.ISSUED, upd.status());
        assertThrows(IllegalArgumentException.class, () -> service.setGenre(b.id(), null));
    }

    @Test
    void removeBook() {
        Book b = service.add("Удаляемая", "Автор", 2010, null);
        service.remove(b.id());
        assertTrue(service.getAll().isEmpty());
        assertThrows(BookNotFoundException.class, () -> service.remove(b.id()));
    }
}
