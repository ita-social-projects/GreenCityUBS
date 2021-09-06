package greencity.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class BagsInfoRepo {
    private final JdbcTemplate jdbcTemplate;
    private static final String QUERY =
        "select name, b.capacity, b.price, obm.amount, (b.price * obm.amount) as summ from bag_translations "
            + "join bag b on bag_translations.bag_id = b.id "
            + "join order_bag_mapping obm on b.id = obm.bag_id "
            + "where obm.order_id =";

    /**
     * method, that returns Bags info.
     *
     * @author Nazar Struk
     */
    public List<Map<String, Object>> getBagInfo(Long orderId) {
        return jdbcTemplate.queryForList(QUERY + orderId);
    }
}
