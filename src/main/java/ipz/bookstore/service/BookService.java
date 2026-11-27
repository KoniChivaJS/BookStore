package ipz.bookstore.service;

import ipz.bookstore.exception.BookNotFoundException;
import ipz.bookstore.model.Book;
import ipz.bookstore.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

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

    // Annotation on a service method: even if a controller lets ADMIN through, only SUPERADMIN deletes
    @PreAuthorize("hasRole('SUPERADMIN')")
    public void delete(String id) {
        Book book = findById(id);
        repository.deleteById(id);

        // Audit of deletes (Task 10): the document is gone, so createdBy/lastModifiedBy can't record it.
        // Write who deleted what and when to the log instead; the user comes from the JWT.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String deletedBy = auth != null ? auth.getName() : "unknown";
        log.info("AUDIT: book id={} title='{}' was deleted by {} at {}",
                id, book.getTitle(), deletedBy, Instant.now());
    }
}
