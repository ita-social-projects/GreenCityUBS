package greencity.repository;

import greencity.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<Order, Integer> {
    /**
     * Method for update Exported value.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Modifying
    @Query(value = "UPDATE ORDER_BAG_MAPPING SET EXPORTED_QUANTITY = :valueExported "
        + "WHERE ORDER_ID = :orderId AND BAG_ID = :bagId", nativeQuery = true)
    void updateExporter(Integer valueExported, Long orderId, Long bagId);

    /**
     * Method for get Amount value.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Lilia Mokhnatska
     */
    @Query(value = "SELECT obm.amount FROM order_bag_mapping as obm "
        + "WHERE obm.order_id = :orderId AND obm.bag_id = :bagId", nativeQuery = true)
    Long getAmount(Long orderId, Long bagId);

    /**
     * Method for update Amount value.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Modifying
    @Query(value = "UPDATE ORDER_BAG_MAPPING SET AMOUNT = :valueAmount "
        + "WHERE ORDER_ID = :orderId AND BAG_ID = :bagId", nativeQuery = true)
    void updateAmount(Integer valueAmount, Long orderId, Long bagId);

    /**
     * Method for update Confirm value.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Modifying
    @Query(value = "UPDATE ORDER_BAG_MAPPING SET CONFIRMED_QUANTITY = :valueConfirmed "
        + "WHERE ORDER_ID = :orderId AND BAG_ID = :bagId", nativeQuery = true)
    void updateConfirm(Integer valueConfirmed, Long orderId, Long bagId);

    /**
     * Method for INSERT new record for ability to update value in next steps.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Modifying
    @Query(value = "INSERT INTO ORDER_BAG_MAPPING (ORDER_ID,BAG_ID) VALUES (:orderId,:bagId)", nativeQuery = true)
    void insertNewRecord(Long orderId, Long bagId);

    /**
     * Method for getting Confirm waste value.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Query(value = "SELECT CONFIRMED_QUANTITY FROM ORDER_BAG_MAPPING "
        + "WHERE ORDER_ID = :orderId AND BAG_ID = :bagId", nativeQuery = true)
    Long getConfirmWaste(Long orderId, Long bagId);

    /**
     * Method for getting Exporter waste value.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Query(value = "SELECT EXPORTED_QUANTITY FROM ORDER_BAG_MAPPING "
        + "WHERE ORDER_ID = :orderId AND BAG_ID = :bagId", nativeQuery = true)
    Long getExporterWaste(Long orderId, Long bagId);

    /**
     * Method for checking if exist record with current order id and bag id.
     *
     * @param orderId order id {@link Long}
     * @param bagId   bag id {@link Long}
     * @author Orest Mahdziak
     */
    @Query(value = "SELECT count(*) FROM ORDER_BAG_MAPPING "
        + "WHERE ORDER_ID = :orderId AND BAG_ID = :bagId", nativeQuery = true)
    Long ifRecordExist(Long orderId, Long bagId);
}