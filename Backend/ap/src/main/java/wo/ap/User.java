package wo.ap;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String gender;
    private Long height;
    private Long weight;

    // Accepted on registration, never serialised back out — returning it
    // leaked the stored password in every API response. Stored BCrypt-hashed.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * The user's IANA timezone (locked decision D2): "today" for quests and
     * streaks is computed in this zone on the server, never on the client.
     */
    @Builder.Default
    private String timezone = "UTC";
}
