package wo.ap;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration payload. Mirrors the constraints the registration form already
 * implies (username 5–10 alphanumeric, height/weight ranges) and adds a
 * minimum password length — BCrypt silently truncates past 72 bytes, so the
 * bound is explicit.
 */
public record RegisterRequest(

        @NotBlank @Size(max = 100) String firstname,

        @Size(max = 100) String lastname,

        @NotBlank @Size(min = 5, max = 10)
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Username must be 5–10 letters or numbers")
        String username,

        @Email @Size(max = 255) String email,

        @Size(max = 20) String gender,

        @Min(50) @Max(250) Long height,

        @Min(2) @Max(300) Long weight,

        @NotBlank @Size(min = 8, max = 72) String password,

        String timezone
) {
}
