package wo.ap;

import org.springframework.data.jpa.repository.JpaRepository;

public interface XpLedgerRepository extends JpaRepository<XpLedger, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
