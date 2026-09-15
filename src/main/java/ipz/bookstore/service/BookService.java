package ipz.bookstore.service;

import ipz.bookstore.exception.BookNotFoundException;
import ipz.bookstore.model.Book;
import ipz.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

    public Book findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public List<Book> search(String title, String author) {
        if (title != null && !title.isBlank()) {
            return repository.findByTitleContainingIgnoreCase(title);
        }
        if (author != null && !author.isBlank()) {
            return repository.findByAuthorContainingIgnoreCase(author);
        }
        return repository.findAll();
    }

    public Book create(Book book) {
        book.setId(null);
        return repository.save(book);
    }

    public Book update(String id, Book changes) {
        Book existing = findById(id);
        existing.setTitle(changes.getTitle());
        existing.setAuthor(changes.getAuthor());
        existing.setGenre(changes.getGenre());
        existing.setIsbn(changes.getIsbn());
        existing.setPrice(changes.getPrice());
        existing.setStock(changes.getStock());
        existing.setYear(changes.getYear());
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
