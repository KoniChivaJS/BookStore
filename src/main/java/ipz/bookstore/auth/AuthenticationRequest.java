package ipz.bookstore.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Login body: { "email": "...", "password": "..." }
public record AuthenticationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

        @NotBlank(message = "Password is required")
        String password) {
}
