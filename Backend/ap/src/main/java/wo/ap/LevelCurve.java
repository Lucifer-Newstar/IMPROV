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
 * Config table (locked decision D6): XP needed to go from {@code level} to
 * {@code level + 1}. Seeded from xpToNext(L) = 100 + 25 × (L − 1). Retune the
 * curve by editing rows, never the code.
 */
@Entity
@Table(name = "level_curve")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelCurve {

    @Id
    private Integer level;

    @Column(name = "xp_to_next", nullable = false)
    private Long xpToNext;
}
