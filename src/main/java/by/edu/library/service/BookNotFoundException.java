package by.edu.library.service;

/**
 * Книги с таким номером нет в каталоге.
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(long id) {
        super("Книга №" + id + " не найдена в каталоге");
    }
}
