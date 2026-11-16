package ipz.bookstore;

import ipz.bookstore.model.Book;
import ipz.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

// Access tests (Lecture 8): check who may call which route, with JUnit + MockMvc instead of .http files.
// Needs MongoDB running (docker start bookstore-mongo), same as the app itself.
@SpringBootTest
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AccessTests {

    private static final String DUMMY_ID = "000000000000000000000000";

    private static final String BOOK_JSON = """
            {
              "title": "Refactoring",
              "author": "Martin Fowler",
              "genre": "Programming",
              "isbn": "9780134757599",
              "price": 47.00,
              "stock": 3,
              "year": 2018
            }
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookRepository repository;

    private MockMvc mockMvc;

    // MockMvc built on the real app context, with the Spring Security filter chain applied
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ---------- Anonymous ----------

    @Test
    @WithAnonymousUser
    @DisplayName("01. Anonymous -> GET /api/books -> 401 Unauthorized")
    void anonymousGetsUnauthorizedOnApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books")).andReturn();

        assertEquals(401, result.getResponse().getStatus());
        assertEquals("Unauthorized", result.getResponse().getErrorMessage());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("02. Anonymous -> GET /index.html (web page, permitAll) -> 200")
    void anonymousCanOpenWebPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/index.html")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    // ---------- /api/hello/admin : hasRole('ADMIN') ----------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("03. ADMIN -> /hello/admin -> 200 + greeting")
    void adminCanOpenAdminRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/admin")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("Hello ADMIN route, you are: admin", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("04. USER -> /hello/admin -> 403 Forbidden")
    void userCannotOpenAdminRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/admin")).andReturn();

        assertEquals(403, result.getResponse().getStatus());
        assertEquals("Forbidden", result.getResponse().getErrorMessage());
    }

    // ---------- /api/hello/user : hasRole('USER') ----------

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("05. USER -> /hello/user -> 200 + greeting")
    void userCanOpenUserRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/user")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("Hello USER route, you are: user", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("06. ADMIN -> /hello/user -> 403 Forbidden")
    void adminCannotOpenUserRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/user")).andReturn();

        assertEquals(403, result.getResponse().getStatus());
        assertEquals("Forbidden", result.getResponse().getErrorMessage());
    }

    // ---------- /api/hello/superadmin : hasRole('SUPERADMIN') ----------

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    @DisplayName("07. SUPERADMIN -> /hello/superadmin -> 200")
    void superadminCanOpenSuperadminRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/superadmin")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("Hello SUPERADMIN route, you are: superadmin", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("08. ADMIN -> /hello/superadmin -> 403")
    void adminCannotOpenSuperadminRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/superadmin")).andReturn();

        assertEquals(403, result.getResponse().getStatus());
    }

    // ---------- /api/hello/staff : hasAnyRole('ADMIN', 'SUPERADMIN') ----------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("09. ADMIN -> /hello/staff -> 200")
    void adminCanOpenStaffRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/staff")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    @DisplayName("10. SUPERADMIN -> /hello/staff -> 200")
    void superadminCanOpenStaffRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/staff")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("11. USER -> /hello/staff -> 403")
    void userCannotOpenStaffRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/staff")).andReturn();

        assertEquals(403, result.getResponse().getStatus());
    }

    // ---------- /api/hello/any : no @PreAuthorize, any logged-in user ----------

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("12. USER -> /hello/any (no annotation) -> 200")
    void userCanOpenAnyRoute() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/any")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    // ---------- Real in-memory users and BCrypt passwords (no mock user) ----------

    @Test
    @DisplayName("13. Basic auth user@bookstore.com/user (real user from MongoDB) -> /hello/user -> 200")
    void realUserCredentialsWork() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/user")
                .with(httpBasic("user@bookstore.com", "user"))).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("14. Basic auth with wrong password -> 401")
    void wrongPasswordIsRejected() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello/user")
                .with(httpBasic("user@bookstore.com", "wrong-password"))).andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // ---------- /api/books ----------

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("15. USER -> GET /api/books -> 200")
    void userCanReadBooks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("16. USER -> POST /api/books -> 403")
    void userCannotCreateBook() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BOOK_JSON)).andReturn();

        assertEquals(403, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("17. ADMIN -> POST /api/books -> 201, audit createdBy = admin")
    void adminCanCreateBook() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BOOK_JSON)).andReturn();

        assertEquals(201, result.getResponse().getStatus());
        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        assertFalse(result.getResponse().getContentAsString().isBlank());

        String id = location.substring(location.lastIndexOf('/') + 1);
        Book saved = repository.findById(id).orElseThrow();
        assertEquals("admin", saved.getCreatedBy());

        repository.deleteById(id); // clean up
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("18. USER -> PUT /api/books/{id} -> 403")
    void userCannotUpdateBook() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/books/" + DUMMY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BOOK_JSON)).andReturn();

        assertEquals(403, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("19. ADMIN -> DELETE /api/books/{id} -> 403 (blocked by @PreAuthorize on BookService)")
    void adminCannotDeleteBook() throws Exception {
        Book book = repository.save(new Book("Temp", "Temp", "Temp", "0", 1, 1, 2000));

        MvcResult result = mockMvc.perform(delete("/api/books/" + book.getId())).andReturn();

        assertEquals(403, result.getResponse().getStatus());
        assertFalse(repository.findById(book.getId()).isEmpty()); // book is still there

        repository.deleteById(book.getId()); // clean up
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    @DisplayName("20. SUPERADMIN -> DELETE /api/books/{id} -> 204, book removed")
    void superadminCanDeleteBook() throws Exception {
        Book book = repository.save(new Book("Temp", "Temp", "Temp", "0", 1, 1, 2000));

        MvcResult result = mockMvc.perform(delete("/api/books/" + book.getId())).andReturn();

        assertEquals(204, result.getResponse().getStatus());
        assertFalse(repository.existsById(book.getId()));
    }
}
