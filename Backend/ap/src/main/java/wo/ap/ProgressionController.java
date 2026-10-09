package wo.ap;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgressionController {

    private final ProgressionService progressionService;
    private final UserRepository userRepository;

    @GetMapping("/progression")
    public ProgressionResponse progression() {
        return progressionService.progression(SecurityUtils.requireUser(userRepository));
    }

    @GetMapping("/ranks")
    public List<RankInfo> ranks() {
        return progressionService.ranks();
    }
}
