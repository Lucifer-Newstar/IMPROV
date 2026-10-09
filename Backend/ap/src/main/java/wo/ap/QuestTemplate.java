package wo.ap;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A quest definition. Users never see this table directly — it is filtered by
 * level and copied onto {@link UserQuest} rows at generation time.
 */
@Entity
@Table(name = "quest_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private QuestType type;

    private Long target;
    private String unit;
    private String statCode;
    private Long statAmount;
    private Long xpReward;
    private Integer minLevel;
}
