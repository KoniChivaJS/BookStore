package ipz.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled: stateless REST API with Basic auth, so POST/PUT/DELETE are allowed
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Hello routes: one per role + shared ones
                        .requestMatchers("/api/hello/user").hasRole("USER")
                        .requestMatchers("/api/hello/admin").hasRole("ADMIN")
                        .requestMatchers("/api/hello/superadmin").hasRole("SUPERADMIN")
                        .requestMatchers("/api/hello/staff").hasAnyRole("ADMIN", "SUPERADMIN")

                        // Books: everyone logged in can read, staff can write, only superadmin deletes
                        .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("SUPERADMIN")

                        // Any other API route: just needs a login, no role check (e.g. /api/hello/any)
                        .requestMatchers("/api/**").authenticated()

                        // Web page (index.html, js, css) is public
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // Three users kept in memory. {noop} = password stored as plain text (no encoder yet).
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}user")
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();

        UserDetails superadmin = User.builder()
                .username("superadmin")
                .password("{noop}superadmin")
                .roles("SUPERADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin, superadmin);
    }
}
