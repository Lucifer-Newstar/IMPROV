package wo.ap;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/overview")
    public OverviewResponse overview() {
        return analyticsService.overview(SecurityUtils.requireUser(userRepository));
    }

    @GetMapping("/streak")
    public StreakResponse streak() {
        return analyticsService.streak(SecurityUtils.requireUser(userRepository));
    }

    @GetMapping("/calendar")
    public CalendarResponse calendar(@RequestParam(required = false) String month) {
        return analyticsService.calendar(SecurityUtils.requireUser(userRepository), month);
    }
}
