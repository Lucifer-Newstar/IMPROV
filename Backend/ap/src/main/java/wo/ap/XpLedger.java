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
 * Append-only XP ledger (locked decision D5). Level, rank and streak are
 * derived from this table and can be fully recomputed — the XP curve can be
 * retuned post-launch without corrupting history. The unique idempotency key
 * makes "award XP for quest X" exactly-once.
 */
@Entity
@Table(name = "xp_ledger",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_xp_ledger_idempotency",
                columnNames = {"idempotency_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XpLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private XpSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    private UserQuest quest;

    @Column(name = "quest_date", nullable = false)
    private LocalDate questDate;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
