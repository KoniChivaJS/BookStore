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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// JWT issuing (Task 8): POST /api/auth/authenticate with a seeded MongoDB user.
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
}
