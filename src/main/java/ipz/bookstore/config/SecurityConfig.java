package ipz.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// Turns on @PreAuthorize: role checks now live on controller/service methods (Task 6)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled: stateless REST API with Basic auth, so POST/PUT/DELETE are allowed
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Task 5 role rules, replaced by @PreAuthorize annotations in Task 6:
                        // .requestMatchers("/api/hello/user").hasRole("USER")
                        // .requestMatchers("/api/hello/admin").hasRole("ADMIN")
                        // .requestMatchers("/api/hello/superadmin").hasRole("SUPERADMIN")
                        // .requestMatchers("/api/hello/staff").hasAnyRole("ADMIN", "SUPERADMIN")
                        // .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                        // .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("SUPERADMIN")

                        // API still needs a login; which role may call what is decided by @PreAuthorize
                        .requestMatchers("/api/**").authenticated()

                        // Web page (index.html, js, css) is public
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // Hashes passwords with BCrypt instead of storing them as plain text ({noop})
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Three users kept in memory, passwords stored as BCrypt hashes
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN")
                .build();

        UserDetails superadmin = User.builder()
                .username("superadmin")
                .password(passwordEncoder.encode("superadmin"))
                .roles("SUPERADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin, superadmin);
    }
}
