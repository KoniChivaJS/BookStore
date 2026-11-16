package ipz.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// Turns on @PreAuthorize: role checks live on controller/service methods (Task 6)
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled: stateless REST API, so POST/PUT/DELETE are allowed
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Login endpoint that issues JWTs must be open to everyone (Task 8)
                        .requestMatchers("/api/auth/**").permitAll()

                        // API still needs a login; which role may call what is decided by @PreAuthorize
                        .requestMatchers("/api/**").authenticated()

                        // Web page (index.html, js, css) is public
                        .anyRequest().permitAll())
                // No HTTP session: every request must carry its own credentials
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Users come from MongoDB via the DAO provider (BeansConfiguration)
                .authenticationProvider(authenticationProvider)
                // Basic auth kept until the JWT filter is added in the next lab
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
