package wo.ap;

/**
 * Principal stored in the security context by {@link JwtAuthFilter}.
 * Carries the user id and username from the JWT claims — no DB lookup needed
 * to authenticate a request.
 */
public record AuthUser(Long id, String username) {
}
