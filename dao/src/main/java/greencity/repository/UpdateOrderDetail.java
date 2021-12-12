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

    /**
     * Method for INSERT new record for ability to update value in next steps.
     */
    public Boolean insertNewRecord(Long orderId, Long bagId) {
        String query = "INSERT INTO ORDER_BAG_MAPPING (ORDER_ID,BAG_ID) VALUES (?,?)";
        return jdbcTemplate.execute(query, (PreparedStatementCallback<Boolean>) ps -> {
            ps.setLong(1, orderId);
            ps.setLong(2, bagId);

            return ps.execute();
        });
    }

    /**
     * Method for getting Confirm waste value.
     */
    public Long getConfirmWaste(Long orderId, Long bagId) {
        String query = "SELECT CONFIRMED_QUANTITY FROM ORDER_BAG_MAPPING "
            + " WHERE ORDER_ID = ? AND BAG_ID = ?";
        return jdbcTemplate.queryForObject(query, new Object[] {orderId, bagId}, Long.class);
    }

    /**
     * Method for getting Exporter waste value.
     */
    public Long getExporterWaste(Long orderId, Long bagId) {
        String query = "SELECT EXPORTED_QUANTITY FROM ORDER_BAG_MAPPING "
            + " WHERE ORDER_ID = ? AND BAG_ID = ?";
        return jdbcTemplate.queryForObject(query, new Object[] {orderId, bagId}, Long.class);
    }

    /**
     * Method for checking if exist record with current order id and bag id.
     */
    public Boolean ifRecordExist(Long orderId, Long bagId) {
        String query = "SELECT count(*) FROM ORDER_BAG_MAPPING WHERE ORDER_ID = ? and BAG_ID=?";
        int trueOrFalse = jdbcTemplate.queryForObject(query, new Object[] {orderId, bagId}, Integer.class);

        return trueOrFalse > 0;
    }
}
