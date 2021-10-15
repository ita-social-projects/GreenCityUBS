package greencity.repository;

import greencity.entity.order.Bag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BagRepository extends JpaRepository<Bag, Integer> {
    /**
     * method, that returns {@link List}of{@link Bag} that have bags by order id.
     *
     * @param id order id
     * @return {@link List}of{@link Bag} by it's language and orderId.
     * @author Mahdziak Orest
     */
    @Query(value = "SELECT * FROM ORDER_BAG_MAPPING as OBM "
        + "JOIN BAG AS B ON OBM.ORDER_ID = :orderId and OBM.BAG_ID = B.ID "
        + "ORDER BY B.ID", nativeQuery = true)
    List<Bag> findBagByOrderId(@Param("orderId") Long id);

    /**
     * This is method which find capacity by id.
     * 
     * @param bagId {@link Integer}.
     * @return {@link Integer}.
     * @author Yuriy Bahlay.
     */
    @Query(value = "SELECT b.capacity FROM Bag AS b WHERE b.id =:bagId")
    Integer findCapacityById(@Param("bagId") Integer bagId);
}
