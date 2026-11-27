package ipz.bookstore.auth;

import ipz.bookstore.security.JwtService;
import ipz.bookstore.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Checks email + password against MongoDB; throws BadCredentialsException if wrong
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = (User) authentication.getPrincipal();

        // Extra data to put into the token (claims)
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", user.getFullName());

        String token = jwtService.generateToken(claims, user);
        return new AuthenticationResponse(token);
    }
}
