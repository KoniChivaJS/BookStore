package ipz.bookstore.config;

import ipz.bookstore.model.Book;
import ipz.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository repository;

    public DataSeeder(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Book("Clean Code", "Robert C. Martin", "Programming",
                        "9780132350884", 39.99, 12, 2008),
                new Book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "Programming",
                        "9780201616224", 45.50, 7, 1999),
                new Book("1984", "George Orwell", "Dystopia",
                        "9780451524935", 12.99, 30, 1949),
                new Book("Kobzar", "Taras Shevchenko", "Poetry",
                        "9789660377271", 15.00, 20, 1840),
                new Book("The Hobbit", "J. R. R. Tolkien", "Fantasy",
                        "9780547928227", 18.75, 15, 1937)
        ));
    }
}
