package greencity.repository;

import greencity.entity.order.Order;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@AllArgsConstructor
public class AllValuesFromTableRepo {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Method returns of undelivered orders.
     */
    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList("select orders.id as orderId, orders.order_status , orders.order_date,"
            + "concat_ws(' ',ubs_user.first_name,ubs_user.last_name) as clientName,"
            + "ubs_user.phone_number ,ubs_user.email,"
            + "users.violations,"
            + "address.district,concat_WS(', ',address.city,address.street,address.house_number,"
            + "address.house_corpus,address.entrance_number) as address,"
            + "users.recipient_name,"
            + "users.recipient_email,"
            + "users.recipient_phone,"
            + "address.comment as comment_To_Address_For_Client,"
            + "(select amount from order_bag_mapping where  bag_id = 1 "
            + "and order_bag_mapping.order_id = orders.id) as garbage_Bags_120_Amount,"
            + "(select amount from order_bag_mapping where  bag_id = 2 "
            + "and order_bag_mapping.order_id = orders.id)as bo_Bags_120_Amount,"
            + "(select amount from order_bag_mapping where  bag_id = 3 "
            + "and order_bag_mapping.order_id = orders.id)as bo_Bags_20_Amount,"
            + "payment.amount as total_Order_Sum,"
            + "certificate.code,certificate.points,(payment.amount-certificate.points)as amount_Due,"
            + "orders.comment as comment_For_Order_By_Client,"
            + "payment.payment_system,"
            + "cast(orders.deliver_from as date) as date_Of_Export,"
            + "concat_Ws('-',cast(orders.deliver_from as time),cast(orders.deliver_to as time)) as time_Of_Export,"
            + "payment.id as id_Order_From_Shop,"
            + "orders.receiving_station,"
            + "orders.note as comments_for_order "
            + "from orders "
            + "left join ubs_user on orders.ubs_user_id = ubs_user.id "
            + "left join users on orders.users_id = users.id "
            + "left join address on ubs_user.address_id = address.id "
            + "left join payment on orders.payment_id = payment.id "
            + "left join certificate on orders.id = certificate.order_id");
    }

    /**
     * Method returns of u.
     */
    public List<Map<String, Object>> findAllEmpl(Long orderId) {
        return jdbcTemplate.queryForList("select concat_ws(' ', first_name, "
            + "last_name) as name,position_id from employees "
            + "left join employee_position on employees.id = employee_position.employee_id "
            + "left join order_employee on employees.id = order_employee.employee_id "
            + "where order_id =" + orderId);
    }
}
