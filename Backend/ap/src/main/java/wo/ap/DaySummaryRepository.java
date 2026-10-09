package wo.ap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DaySummaryRepository extends JpaRepository<DaySummary, Long> {

    Optional<DaySummary> findByUserIdAndQuestDate(Long userId, LocalDate questDate);

    List<DaySummary> findByUserIdOrderByQuestDateAsc(Long userId);

    List<DaySummary> findByUserIdAndQuestDateBetweenOrderByQuestDateAsc(Long userId, LocalDate start, LocalDate end);

    List<DaySummary> findByUserIdAndQuestsCompletedGreaterThanOrderByQuestDateDesc(Long userId, long minQuests);

    long countByUserIdAndQuestsCompletedGreaterThan(Long userId, long minQuests);
}
