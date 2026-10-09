package wo.ap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * One row per user per day: XP earned and quests completed. Streaks are always
 * derived from this table, never mutated directly (mechanics spec §6.4/§6.6).
 */
@Entity
@Table(name = "day_summaries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_day_summaries_user_date",
                columnNames = {"user_id", "quest_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DaySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "quest_date", nullable = false)
    private LocalDate questDate;

    @Builder.Default
    private Long xpEarned = 0L;

    @Builder.Default
    private Long questsCompleted = 0L;
}
