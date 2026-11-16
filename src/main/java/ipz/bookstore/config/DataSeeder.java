package ipz.bookstore.config;

import ipz.bookstore.model.Book;
import ipz.bookstore.repository.BookRepository;
import ipz.bookstore.user.Role;
import ipz.bookstore.user.User;
import ipz.bookstore.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(BookRepository repository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
    }

    // Users live in MongoDB now (Task 8); passwords stored as BCrypt hashes
    private void seedUsers() {
        saveUserIfMissing("John", "Smith", "john@mail.com", "password", Role.ADMIN);
        saveUserIfMissing("Uma", "User", "user@bookstore.com", "user", Role.USER);
        saveUserIfMissing("Adam", "Admin", "admin@bookstore.com", "admin", Role.ADMIN);
        saveUserIfMissing("Sam", "Super", "superadmin@bookstore.com", "superadmin", Role.SUPERADMIN);
    }

    private void saveUserIfMissing(String firstName, String lastName, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(new User(firstName, lastName, email,
                passwordEncoder.encode(rawPassword), List.of(role)));
    }

    private void seedBooks() {
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
