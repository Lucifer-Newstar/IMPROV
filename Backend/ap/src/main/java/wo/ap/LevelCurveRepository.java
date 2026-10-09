package wo.ap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelCurveRepository extends JpaRepository<LevelCurve, Integer> {

    List<LevelCurve> findAllByOrderByLevelAsc();
}
