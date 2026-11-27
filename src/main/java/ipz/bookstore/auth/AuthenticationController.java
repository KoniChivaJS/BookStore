package ipz.bookstore.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// POST /api/auth/authenticate -> JWT. Open to everyone (permitAll in SecurityConfig).
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return service.authenticate(request);
    }
}
