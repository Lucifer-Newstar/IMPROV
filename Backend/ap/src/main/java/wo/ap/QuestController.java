package wo.ap;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final UserRepository userRepository;

    @GetMapping("/today")
    public QuestBoardResponse today() {
        return questService.todayBoard(SecurityUtils.requireUser(userRepository));
    }

    @PostMapping("/{id}/log")
    public UserQuestDto log(@PathVariable long id, @Valid @RequestBody LogQuestRequest request) {
        return questService.log(SecurityUtils.requireUser(userRepository), id, request.amount());
    }

    @PostMapping("/{id}/complete")
    public UserQuestDto complete(@PathVariable long id) {
        return questService.complete(SecurityUtils.requireUser(userRepository), id);
    }
}
