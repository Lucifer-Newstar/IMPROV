package wo.ap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The rank ladder (E → S). The XP multiplier is applied to quest XP awards and
 * is deliberately modest (max ×1.75) — the reference platform's ×5.0 on a
 * linear curve made late levels *faster*, which is backwards.
 *
 * The column is rank_code, not rank: RANK is a reserved word in MySQL 8.
 */
@Entity
@Table(name = "ranks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankTier {

    @Id
    @Column(name = "rank_code")
    private String rank;

    @Column(name = "min_level", nullable = false)
    private Integer minLevel;

    @Column(name = "max_level")
    private Integer maxLevel;

    private String color;

    @Column(name = "xp_multiplier", nullable = false)
    private Double xpMultiplier;
}
