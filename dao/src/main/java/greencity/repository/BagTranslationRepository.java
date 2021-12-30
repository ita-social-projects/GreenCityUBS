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
     * method, that returns {@link List}of{@link BagTranslation} that have language.
     *
     * @param language code of the language.
     * @return {@link List}of{@link BagTranslation} by it's language.
     * @author Veremchuk Zahar
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM bag_translations b LEFT JOIN languages l ON b.language_id = l.id "
            + "WHERE l.code = :language")
    List<BagTranslation> findAllByLanguage(String language);

    /**
     * method, that returns {@link List}of{@link BagTranslation} that have language.
     *
     * @param language code of the language.
     * @param orderId  order id
     * @return {@link List}of{@link BagTranslation} by it's language and orderId.
     * @author Mahdziak Orest
     */

    @Query(nativeQuery = true,
        value = "SELECT * FROM ORDER_BAG_MAPPING AS OBM JOIN BAG AS B ON OBM.BAG_ID = B.ID "
            + "JOIN BAG_TRANSLATIONS AS BT ON B.ID = BT.BAG_ID "
            + "JOIN LANGUAGES AS L ON BT.LANGUAGE_ID = L.ID "
            + "WHERE L.CODE = :language AND OBM.ORDER_ID = :orderId")
    List<BagTranslation> findAllByLanguageOrder(String language, @Param("orderId") Long orderId);

    /**
     * Method,that return Bag translation by {@link Bag} and {@link String} -
     * language code.
     * 
     * @param bag  - current Bag.
     * @param code - code of the language.
     * @return BagTranslation with current bag and current lang code.
     */
    BagTranslation findBagTranslationByBagAndLanguageCode(Bag bag, String code);

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
     * @param bagId    {@link Integer}.
     * @param language {@link String}.
     * @return {@link StringBuilder}.
     * @author Yuriy Bahlay.
     */
    @Query(value = "SELECT b.name FROM BagTranslation AS b WHERE b.bag.id =:bagId AND b.language.id =:language")
    StringBuilder findNameByBagId(@Param("bagId") Integer bagId, @Param("language") Long language);
}
