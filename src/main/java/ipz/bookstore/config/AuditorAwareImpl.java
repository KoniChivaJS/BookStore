package ipz.bookstore.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

// Tells Spring Data who the "current auditor" is, for createdBy / lastModifiedBy.
// The user comes from the JWT: JwtAuthenticationFilter puts it into the SecurityContext,
// and getName() returns the token's subject (the email). Also works for Basic auth.
// "system" for writes made outside an HTTP request (e.g. DataSeeder on startup).
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }
        return Optional.of(auth.getName());
    }
}
