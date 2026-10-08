package wo.ap;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Single origin: nginx proxies /api to this service, so the frontend never
// makes a cross-origin request and no CORS headers are needed anywhere.
// (The old @CrossOrigin(origins = "*") was a development crutch.)
@RestController
@RequestMapping("/api/Users")
@RequiredArgsConstructor
public class Controller {

    private final service userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        try {
            User saved = userService.save(user);
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException e) {
            // Most likely uk_users_username: the username is already taken.
            // A client error is a 409, not a 500.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request.getUsername(), request.getPassword()));
    }

}
