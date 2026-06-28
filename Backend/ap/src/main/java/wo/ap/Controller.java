package wo.ap;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class Controller {

    private final service userService;

    @PostMapping("/register")
    public ResponseEntity<User> register (@RequestBody User user){
        User save = userService.save(user);
        return ResponseEntity.ok(save);
    }
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request.getUsername(), request.getPassword()));
    }


}
