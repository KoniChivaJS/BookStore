package ipz.bookstore.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

// Test routes for role-based access. Who may call each one is set by @PreAuthorize.
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public String helloUser(Principal principal) {
        return "Hello USER route, you are: " + principal.getName();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String helloAdmin(Principal principal) {
        return "Hello ADMIN route, you are: " + principal.getName();
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/superadmin")
    public String helloSuperadmin(Principal principal) {
        return "Hello SUPERADMIN route, you are: " + principal.getName();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping("/staff")
    public String helloStaff(Principal principal) {
        return "Hello STAFF route (admin or superadmin), you are: " + principal.getName();
    }

    // No @PreAuthorize: any logged-in user may call it
    @GetMapping("/any")
    public String helloAny(Principal principal) {
        return "Hello ANY logged-in user, you are: " + principal.getName();
    }
}
