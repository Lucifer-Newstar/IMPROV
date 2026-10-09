package wo.ap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.Instant;
import java.time.LocalDate;

/**
 * One user's quest for one day. The template's target, rewards and stats are
 * snapshotted onto the row at generation time (locked decision D7): templates
 * change, history must not rewrite itself.
 */
@Entity
@Table(name = "user_quests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_quests_day_template",
                columnNames = {"user_id", "quest_date", "template_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "quest_date", nullable = false)
    private LocalDate questDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private QuestTemplate template;

    // ---- snapshot of the template at generation time ----
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private QuestType type;

    private Long target;
    private String unit;
    private String statCode;
    private Long statAmount;
    private Long xpReward;

    // ---- progress ----
    @Builder.Default
    private Long logged = 0L;

    @Builder.Default
    private boolean completed = false;

    private Long awardedXp;
    private Instant completedAt;
}
