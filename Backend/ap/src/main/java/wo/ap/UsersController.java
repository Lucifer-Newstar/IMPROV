package wo.ap;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserProfile> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserProfile.of(user));
    }

    /** On success the auth cookies are set; the response is the profile only. */
    @PostMapping("/login")
    public ResponseEntity<UserProfile> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {
        User user = userService.authenticate(request.username(), request.password());
        authService.writeCookies(response, authService.issueTokens(user));
        return ResponseEntity.ok(UserProfile.of(user));
    }
}
