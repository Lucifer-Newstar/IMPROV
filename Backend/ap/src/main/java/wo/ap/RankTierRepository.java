package wo.ap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankTierRepository extends JpaRepository<RankTier, String> {

    List<RankTier> findAllByOrderByMinLevelAsc();

    Optional<RankTier> findByRank(String rank);
}
