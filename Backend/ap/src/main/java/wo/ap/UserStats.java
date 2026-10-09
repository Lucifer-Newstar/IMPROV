package wo.ap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cached, derived progression state for one user: total XP, level, rank and
 * both streaks. Updated in the same transaction that writes the XP ledger, so
 * it is never stale — and always recomputable from the ledger and
 * day_summaries (locked decision D5).
 *
 * The column is rank_code, not rank: RANK is a reserved word in MySQL 8.
 */
@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private Long totalXp = 0L;

    @Builder.Default
    private Integer level = 1;

    @Column(name = "rank_code")
    private String rank;

    @Builder.Default
    private Long currentStreak = 0L;

    @Builder.Default
    private Long bestStreak = 0L;
}
