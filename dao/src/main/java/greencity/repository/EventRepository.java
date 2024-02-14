package greencity.repository;

import greencity.entity.order.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    /**
     * Method which is return list of events from some order b id.
     *
     * @return {@link List}.
     */
    @Query("SELECT e FROM Event AS e "
        + "INNER JOIN Order AS o "
        + "ON o.id = e.order.id "
        + "WHERE o.id = :OrderId")
    List<Event> findAllEventsByOrderId(@Param(value = "OrderId") Long id);
}
