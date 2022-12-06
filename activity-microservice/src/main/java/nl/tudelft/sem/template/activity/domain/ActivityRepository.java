package nl.tudelft.sem.template.activity.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for quering and persisting user aggregate roots.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
    /**
     * Find activity by name.
     */
    Optional<Activity> findActivityByName(String activityName);

    boolean existsActivityBy(int activityId);
}