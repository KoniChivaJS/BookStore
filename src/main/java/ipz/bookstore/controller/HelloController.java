package ipz.bookstore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

// Test routes for role-based access. Who may call each one is set in SecurityConfig.
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping("/user")
    public String helloUser(Principal principal) {
        return "Hello USER route, you are: " + principal.getName();
    }

    @GetMapping("/admin")
    public String helloAdmin(Principal principal) {
        return "Hello ADMIN route, you are: " + principal.getName();
    }

    @GetMapping("/superadmin")
    public String helloSuperadmin(Principal principal) {
        return "Hello SUPERADMIN route, you are: " + principal.getName();
    }

    @GetMapping("/staff")
    public String helloStaff(Principal principal) {
        return "Hello STAFF route (admin or superadmin), you are: " + principal.getName();
    }

    @GetMapping("/any")
    public String helloAny(Principal principal) {
        return "Hello ANY logged-in user, you are: " + principal.getName();
    }
}
