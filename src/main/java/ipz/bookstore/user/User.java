package ipz.bookstore.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Application user stored in MongoDB (Task 8). Replaces the in-memory users from Task 5.
// Logs in by email, so getUsername() returns the email.
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String email;

    // BCrypt hash, never the raw password
    private String password;

    private boolean accountLocked;

    private boolean enabled;

    private List<Role> roles;

    public User() {
    }

    public User(String firstName, String lastName, String email, String password, List<Role> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.accountLocked = false;
        this.enabled = true;
    }

    // Used as an extra JWT claim
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // "ROLE_" prefix so hasRole('ADMIN') in @PreAuthorize matches
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRoles() {
        return roles;
    }
}
