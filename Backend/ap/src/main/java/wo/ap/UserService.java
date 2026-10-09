package wo.ap;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final PasswordEncoder passwordEncoder;
    private final MeterRegistry meterRegistry;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "That username is already taken");
        }
        User user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .username(request.username())
                .email(blankToNull(request.email()))
                .gender(request.gender())
                .height(request.height())
                .weight(request.weight())
                .password(passwordEncoder.encode(request.password()))
                .timezone(validTimezone(request.timezone()))
                .build();
        User saved = userRepository.save(user);
        // Every user starts with an empty progression cache; it is refreshed
        // on the first XP award and every streak-affecting write.
        userStatsRepository.save(UserStats.builder().user(saved).build());
        meterRegistry.counter("improv.users.registered").increment();
        return saved;
    }

    /** @throws ApiException 401 on bad credentials — never a 500 */
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return user;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /** Falls back to UTC for anything that is not a valid IANA zone id. */
    static String validTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return "UTC";
        }
        try {
            return ZoneId.of(timezone).getId();
        } catch (Exception e) {
            return "UTC";
        }
    }
}
