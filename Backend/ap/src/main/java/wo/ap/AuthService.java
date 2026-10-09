package wo.ap;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token issuing and the auth cookies (locked decision D1).
 *
 * Both tokens travel as httpOnly, SameSite=Strict cookies: JavaScript cannot
 * read them, and cross-site requests cannot send them. The browser attaches
 * them automatically, so the frontend never stores or handles a token.
 */
@Service
public class AuthService {

    public static final String ACCESS_COOKIE = "improv_access";
    public static final String REFRESH_COOKIE = "improv_refresh";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            @Value("${improv.auth.cookie-secure}") boolean cookieSecure,
            @Value("${improv.auth.cookie-same-site}") String cookieSameSite) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    public record TokenPair(String access, String refresh) {
    }

    public TokenPair issueTokens(User user) {
        return new TokenPair(
                jwtService.issueAccessToken(user.getId(), user.getUsername()),
                jwtService.issueRefreshToken(user.getId(), user.getUsername()));
    }

    public void writeCookies(HttpServletResponse response, TokenPair pair) {
        writeCookie(response, ACCESS_COOKIE, pair.access(), jwtService.accessTtl());
        writeCookie(response, REFRESH_COOKIE, pair.refresh(), jwtService.refreshTtl());
    }

    public void clearCookies(HttpServletResponse response) {
        writeCookie(response, ACCESS_COOKIE, "", Duration.ZERO);
        writeCookie(response, REFRESH_COOKIE, "", Duration.ZERO);
    }

    private void writeCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Validates a refresh token and returns the user it belongs to.
     * Rotation is stateless: the old refresh token stays valid until it
     * expires — acceptable for an MVP, noted in docs/DEVOPS.md.
     */
    public User refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parse(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }
        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid");
        }
        Long userId = claims.get("uid", Long.class);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }

    public User currentUser() {
        return SecurityUtils.requireUser(userRepository);
    }
}
