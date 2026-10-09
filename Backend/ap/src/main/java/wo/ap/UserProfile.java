package wo.ap;

/**
 * The user as the API returns it. The password field is deliberately absent —
 * profiles are the only shape user data leaves the server in.
 */
public record UserProfile(
        Long id,
        String firstname,
        String lastname,
        String username,
        String email,
        String gender,
        Long height,
        Long weight,
        String timezone
) {
    public static UserProfile of(User user) {
        return new UserProfile(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getUsername(),
                user.getEmail(),
                user.getGender(),
                user.getHeight(),
                user.getWeight(),
                user.getTimezone());
    }
}
