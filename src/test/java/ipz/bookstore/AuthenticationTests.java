package ipz.bookstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import ipz.bookstore.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

// JWT issuing (Task 8) and using the token as "Authorization: Bearer <JWT>" (Task 9).
@SpringBootTest
class AuthenticationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Valid email + password -> 200 and a signed JWT with sub, fullName, authorities")
    void validCredentialsReturnToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@mail.com\",\"password\":\"password\"}")).andReturn();

        assertEquals(200, result.getResponse().getStatus());

        String token = new ObjectMapper()
                .readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        // Throws if the signature does not match our secret key
        Claims claims = Jwts.parser()
                .verifyWith(jwtService.getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("john@mail.com", claims.getSubject());
        assertEquals("John Smith", claims.get("fullName"));
        assertEquals(List.of("ROLE_ADMIN"), claims.get("authorities"));
    }

    @Test
    @DisplayName("Wrong password -> 401")
    void wrongPasswordIsRejected() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@mail.com\",\"password\":\"wrong-password\"}")).andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // ---------- Task 9: Bearer token on protected routes ----------

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")).andReturn();
        return new ObjectMapper()
                .readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    @DisplayName("Bearer ADMIN token -> /hello/admin -> 200, logged in as the token's user")
    void adminTokenOpensAdminRoute() throws Exception {
        String token = loginAndGetToken("admin@bookstore.com", "admin");

        MvcResult result = mockMvc.perform(get("/api/hello/admin")
                .header("Authorization", "Bearer " + token)).andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("Hello ADMIN route, you are: admin@bookstore.com", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Bearer USER token -> /hello/admin -> 403")
    void userTokenCannotOpenAdminRoute() throws Exception {
        String token = loginAndGetToken("user@bookstore.com", "user");

        MvcResult result = mockMvc.perform(get("/api/hello/admin")
                .header("Authorization", "Bearer " + token)).andReturn();

        assertEquals(403, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Token with a changed payload (signature no longer matches) -> 401")
    void tamperedTokenIsRejected() throws Exception {
        String token = loginAndGetToken("user@bookstore.com", "user");
        String[] parts = token.split("\\.");
        // Swap in a payload that claims to be the superadmin, keep the user's signature
        String fakePayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"superadmin@bookstore.com\"}".getBytes());
        String forged = parts[0] + "." + fakePayload + "." + parts[2];

        MvcResult result = mockMvc.perform(get("/api/hello/superadmin")
                .header("Authorization", "Bearer " + forged)).andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Expired token (correctly signed) -> 401")
    void expiredTokenIsRejected() throws Exception {
        long hourAgo = System.currentTimeMillis() - 3_600_000;
        String expired = Jwts.builder()
                .subject("admin@bookstore.com")
                .issuedAt(new Date(hourAgo - 1000))
                .expiration(new Date(hourAgo))
                .signWith(jwtService.getSignInKey())
                .compact();

        MvcResult result = mockMvc.perform(get("/api/hello/admin")
                .header("Authorization", "Bearer " + expired)).andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // ---------- Task 10: audit fields come from the JWT user ----------

    @Test
    @DisplayName("Audit: ADMIN token creates, SUPERADMIN token updates and deletes -> createdBy/lastModifiedBy from JWT")
    void auditFieldsComeFromToken() throws Exception {
        String adminToken = loginAndGetToken("admin@bookstore.com", "admin");
        String superToken = loginAndGetToken("superadmin@bookstore.com", "superadmin");
        String book = "{\"title\":\"Effective Java\",\"author\":\"Joshua Bloch\",\"price\":45,\"stock\":4,\"year\":2018}";
        ObjectMapper json = new ObjectMapper();

        MvcResult created = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(book)).andReturn();
        assertEquals(201, created.getResponse().getStatus());
        var createdBody = json.readTree(created.getResponse().getContentAsString());
        String id = createdBody.get("id").asText();
        assertEquals("admin@bookstore.com", createdBody.get("createdBy").asText());

        MvcResult updated = mockMvc.perform(put("/api/books/" + id)
                .header("Authorization", "Bearer " + superToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(book)).andReturn();
        var updatedBody = json.readTree(updated.getResponse().getContentAsString());
        assertEquals("admin@bookstore.com", updatedBody.get("createdBy").asText());
        assertEquals("superadmin@bookstore.com", updatedBody.get("lastModifiedBy").asText());

        MvcResult deleted = mockMvc.perform(delete("/api/books/" + id)
                .header("Authorization", "Bearer " + superToken)).andReturn();
        assertEquals(204, deleted.getResponse().getStatus());
    }
}
