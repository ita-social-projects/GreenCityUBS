package greencity.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UpdateOrderDetail {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Method for update Exported value.
     */
    public void updateExporter(Integer valueExported, Long orderId, Long bagId) {
        jdbcTemplate.execute("UPDATE ORDER_BAG_MAPPING SET EXPORTED_QUANTITY = " + valueExported + " "
            + "WHERE ORDER_ID =" + orderId + " " + "AND BAG_ID =" + bagId + "");
    }

    /**
     * Method for update Amount value.
     */
    public void updateAmount(Integer valueAmount, Long orderId, Long bagId) {
        jdbcTemplate.execute("UPDATE ORDER_BAG_MAPPING SET AMOUNT = " + valueAmount + " "
            + "WHERE ORDER_ID =" + orderId + " " + "AND BAG_ID =" + bagId + "");
    }

    /**
     * Method for update Confirm value.
     */
    public void updateConfirm(Integer valueConfirm, Long orderId, Long bagId) {
        jdbcTemplate.execute("UPDATE ORDER_BAG_MAPPING SET CONFIRMED_QUANTITY = " + valueConfirm + " "
            + "WHERE ORDER_ID =" + orderId + " " + "AND BAG_ID =" + bagId + "");
    }
}
