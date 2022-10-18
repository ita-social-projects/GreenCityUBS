package greencity.repository;

import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Provides an interface to manage {@link BagTranslation} entity.
 *
 * @author Veremchuk Zahar
 */
public interface BagTranslationRepository extends JpaRepository<BagTranslation, Long> {
    /**
     * method, that returns {@link List}of{@link BagTranslation}.
     *
     * @param orderId order id
     * @return {@link List}of{@link BagTranslation} by orderId.
     * @author Max Boiarchuk
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM ORDER_BAG_MAPPING AS OBM JOIN BAG AS B ON OBM.BAG_ID = B.ID "
            + "JOIN BAG_TRANSLATIONS AS BT ON B.ID = BT.BAG_ID "
            + "WHERE OBM.ORDER_ID = :orderId")
    List<BagTranslation> findAllByOrder(@Param("orderId") Long orderId);

    /**
     * Method for get bag translation from bag.
     *
     * @param bag {@link Bag}
     * @return {@link BagTranslation}
     */
    BagTranslation findBagTranslationByBag(Bag bag);

    /**
     * This is method which type of bag.
     *
     * @param bagId {@link Integer}.
     * @return {@link BagTranslation}.
     * @author Yuriy Bahlay.
     */
    BagTranslation findBagTranslationByBagId(Integer bagId);
}
