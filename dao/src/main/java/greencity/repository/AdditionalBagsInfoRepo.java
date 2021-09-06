package greencity.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class AdditionalBagsInfoRepo {
    private final JdbcTemplate jdbcTemplate;
    private static final String QUERY2 = "select distinct u.recipient_name , u.recipient_phone , "
        + "u.recipient_email, a.city , a.street , a.house_number , a.district , a.comment, ";

    /**
     * method, that returns Bags additional info.
     *
     * @author Nazar Struk
     */
    public List<Map<String, Object>> getAdditionalBagInfo(Long orderId, String recipientEmail) {
        return jdbcTemplate.queryForList(QUERY2
            + "(select string_agg(payment_id::text,',')as pay_id "
            + "from payment where recipient_email = ?) "
            + "from payment "
            + "join orders o on o.id = payment.order_id "
            + "join users u on u.id = o.users_id "
            + "join address a on u.id = a.user_id "
            + "where order_id =?", recipientEmail, orderId);
    }
}
