package wo.ap;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

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

    // Accepted on register/login requests, never serialised back out.
    // Returning it leaked the stored password in every API response.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
