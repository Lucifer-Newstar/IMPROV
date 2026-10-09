package wo.ap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    List<UserQuest> findByUserIdAndQuestDateOrderByIdAsc(Long userId, LocalDate questDate);

    Optional<UserQuest> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndCompletedTrue(Long userId);

    @Query("select q.statCode, coalesce(sum(q.statAmount), 0) from UserQuest q "
            + "where q.user.id = :userId and q.completed = true group by q.statCode")
    List<Object[]> sumStatAmountByUserIdGroupedByStatCode(@Param("userId") Long userId);

    @Query("select coalesce(sum(q.logged), 0) from UserQuest q "
            + "where q.user.id = :userId and q.completed = true and q.type = :type")
    long sumLoggedByUserIdAndCompletedType(@Param("userId") Long userId, @Param("type") QuestType type);
}
