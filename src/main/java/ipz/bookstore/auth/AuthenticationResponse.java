package ipz.bookstore.auth;

// Login result: { "token": "<JWT>" }
public record AuthenticationResponse(String token) {
}
