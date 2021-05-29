package greencity.repository;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class AllValuesFromTableRepo {
    private final JdbcTemplate jdbcTemplate;
    private static final String QUERY = "select orders.id as orderId, orders.order_status , orders.order_date,\n"
        + "concat_ws(' ',ubs_user.first_name,ubs_user.last_name) as clientName,"
        + "ubs_user.phone_number ,ubs_user.email,"
        + "users.violations,"
        + "address.district,concat_WS(', ',address.city,address.street,address.house_number,"
        + "address.house_corpus,address.entrance_number) as address,\n"
        + "users.recipient_name,"
        + "users.recipient_email,"
        + "users.recipient_phone,"
        + "address.comment as comment_To_Address_For_Client,\n"
        + "(select amount from order_bag_mapping where  bag_id = 1 "
        + "and order_bag_mapping.order_id = orders.id) as garbage_Bags_120_Amount,\n"
        + "(select amount from order_bag_mapping where  bag_id = 2 "
        + "and order_bag_mapping.order_id = orders.id)as bo_Bags_120_Amount,\n"
        + "(select amount from order_bag_mapping where  bag_id = 3 "
        + "and order_bag_mapping.order_id = orders.id)as bo_Bags_20_Amount,\n"
        + "payment.amount as total_Order_Sum,\n"
        + "certificate.code,certificate.points,(payment.amount-certificate.points)as amount_Due,\n"
        + "orders.comment as comment_For_Order_By_Client,\n"
        + "payment.payment_system,\n"
        + "cast(orders.deliver_from as date) as date_Of_Export,\n"
        + "concat_Ws('-',cast(orders.deliver_from as time),cast(orders.deliver_to as time)) as time_Of_Export,\n"
        + "payment.id as id_Order_From_Shop,\n"
        + "orders.receiving_station,\n"
        + "orders.note as comments_for_order\n"
        + "from orders\n"
        + "left join ubs_user on orders.ubs_user_id = ubs_user.id\n"
        + "left join users on orders.users_id = users.id\n"
        + "left join address on ubs_user.address_id = address.id\n"
        + "left join payment on orders.payment_id = payment.id\n"
        + "left join certificate on orders.id = certificate.order_id";

    private static final String EMPLOYEEQUERY = "select concat_ws(' ', first_name, "
        + "last_name) as name,position_id from employees\n"
        + "left join employee_position on employees.id = employee_position.employee_id\n"
        + "left join order_employee on employees.id = order_employee.employee_id\n"
        + "where order_id =";

    /**
     * Method for finding elements from Order Table without employee.
     */
    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList(QUERY);
    }

    /**
     * Method for finding all employee.
     */
    public List<Map<String, Object>> findAllEmpl(Long orderId) {
        return jdbcTemplate.queryForList(EMPLOYEEQUERY + orderId);
    }

    /**
     * Method for finding elements from Order Table without employee with the
     * sorting possibility.
     */
    public List<Map<String, Object>> findAllWithSorting(String column, String sortingType) {
        return jdbcTemplate.queryForList(QUERY + " order by " + column + " " + sortingType);
    }
}
