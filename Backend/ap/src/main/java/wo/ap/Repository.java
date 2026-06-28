package wo.ap;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface Repository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);
}