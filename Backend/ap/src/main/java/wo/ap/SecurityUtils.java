package wo.ap;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Helpers for reading the authenticated principal from the security context. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser user)) {
            // Only reachable if the filter chain is misconfigured; the
            // exception handler maps this to 401.
            throw new IllegalStateException("No authenticated user in the security context");
        }
        return user;
    }

    /** Loads the full {@link User} entity for the authenticated principal. */
    public static User requireUser(UserRepository userRepository) {
        AuthUser principal = currentUser();
        return userRepository.findById(principal.id())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }
}
