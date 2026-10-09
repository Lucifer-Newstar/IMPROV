package wo.ap;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Issues and parses the stateless JWTs (locked decision D1).
 *
 * Access tokens are short-lived; refresh tokens are long-lived and only ever
 * travel inside an httpOnly cookie, so JavaScript cannot read or steal them.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(
            @Value("${improv.jwt.secret}") String secret,
            @Value("${improv.jwt.access-ttl}") Duration accessTtl,
            @Value("${improv.jwt.refresh-ttl}") Duration refreshTtl) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "improv.jwt.secret must be at least 32 bytes for HS256. Set the JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public String issueAccessToken(Long userId, String username) {
        return issue(userId, username, "access", accessTtl);
    }

    public String issueRefreshToken(Long userId, String username) {
        return issue(userId, username, "refresh", refreshTtl);
    }

    private String issue(Long userId, String username, String type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("typ", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** @throws JwtException if the signature, type or expiry is invalid */
    public Claims parse(String token) throws JwtException {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Duration accessTtl() {
        return accessTtl;
    }

    public Duration refreshTtl() {
        return refreshTtl;
    }
}
