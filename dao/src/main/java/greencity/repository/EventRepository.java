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

    /**
     * Checks if the status of the order was changed from {@code FORMED} to
     * {@code CANCELLED}.
     *
     * @param orderId {@link Long} the ID of the order to check.
     * @return {@code true} if the order status was changed from {@code FORMED} to
     *         {@code CANCELLED}; {@code false} otherwise.
     */
    @Query("SELECT EXISTS ( "
        + "SELECT 1 "
        + "FROM Event e1 "
        + "JOIN Event e2 ON e1.order.id = e2.order.id "
        + "WHERE e1.order.id = :orderId "
        + "AND e1.eventName = 'Статус Замовлення - Сформовано' "
        + "AND e2.eventName = 'Статус Замовлення - Скасовано' "
        + "AND e1.eventDate < e2.eventDate)")
    Boolean wasOrderStatusChangedFromFormedToCanceled(@Param("orderId") Long orderId);
}
