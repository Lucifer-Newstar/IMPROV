package wo.ap;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class service {

    private final Repository userRepository;

    public User save(User user){

        return userRepository.save(user);
    }
    public User login(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
    }
}
