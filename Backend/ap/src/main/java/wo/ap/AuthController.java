package wo.ap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Rotates the token pair. Called by the frontend when an access token expires. */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = JwtAuthFilter.readCookie(request, AuthService.REFRESH_COOKIE);
        if (token == null) {
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "No refresh token");
        }
        var user = authService.refresh(token);
        authService.writeCookies(response, authService.issueTokens(user));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.clearCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserProfile me() {
        return UserProfile.of(authService.currentUser());
    }
}
