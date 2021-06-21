package greencity.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UpdateOrderDetail {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Method for update Exported value.
     *
     * @return
     */

    public Boolean updateExporter(Integer valueExported, Long orderId, Long bagId) {
        String query = "UPDATE ORDER_BAG_MAPPING SET EXPORTED_QUANTITY = ? WHERE ORDER_ID = ? AND BAG_ID = ?";
        return jdbcTemplate.execute(query, (PreparedStatementCallback<Boolean>) ps -> {
            ps.setInt(1, valueExported);
            ps.setLong(2, orderId);
            ps.setLong(3, bagId);

            return ps.execute();
        });
    }

    /**
     * Method for update Amount value.
     */
    public Boolean updateAmount(Integer valueAmount, Long orderId, Long bagId) {
        String query = "UPDATE ORDER_BAG_MAPPING SET AMOUNT = ? WHERE ORDER_ID = ? AND BAG_ID = ?";
        return jdbcTemplate.execute(query, (PreparedStatementCallback<Boolean>) ps -> {
            ps.setInt(1, valueAmount);
            ps.setLong(2, orderId);
            ps.setLong(3, bagId);

            return ps.execute();
        });
    }

    /**
     * Method for update Confirm value.
     */
    public Boolean updateConfirm(Integer valueConfirmed, Long orderId, Long bagId) {
        String query = "UPDATE ORDER_BAG_MAPPING SET CONFIRMED_QUANTITY = ? WHERE ORDER_ID = ? AND BAG_ID = ?";
        return jdbcTemplate.execute(query, (PreparedStatementCallback<Boolean>) ps -> {
            ps.setInt(1, valueConfirmed);
            ps.setLong(2, orderId);
            ps.setLong(3, bagId);

            return ps.execute();
        });
    }
}
