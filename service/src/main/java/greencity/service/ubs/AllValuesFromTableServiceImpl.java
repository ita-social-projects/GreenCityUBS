package greencity.service.ubs;

import greencity.client.RestClient;
import greencity.dto.GetAllFieldsMainDto;
import greencity.dto.UbsCustomersDto;
import greencity.entity.allfieldsordertable.GetAllValuesFromTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AllValuesFromTableServiceImpl implements AllValuesFromTableService {
    private final JdbcTemplate jdbcTemplate;
    private final RestClient restClient;
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
     * {@inheritDoc}
     */
    public List<GetAllFieldsMainDto> findAllValues() {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(QUERY);
        List<Map<String, Object>> employees;
        List<GetAllFieldsMainDto> result = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            employees = jdbcTemplate.queryForList(EMPLOYEEQUERY + map.get("orderid"));
            GetAllValuesFromTable ourTable = GetAllValuesFromTable.builder()
                .orderId((Long) map.get("orderid"))
                .orderStatus((String) map.get("order_status"))
                .orderDate(map.get("order_date").toString())
                .clientName((String) map.get("clientname"))
                .phoneNumber((String) map.get("phone_number"))
                .email((String) map.get("email"))
                .violationsAmount((Integer) map.get("violations"))
                .district((String) map.get("district"))
                .address((String) map.get("address"))
                .recipientName((String) map.get("recipient_name"))
                .emailRecipient((String) map.get("recipient_email"))
                .phoneNumberRecipient((String) map.get("recipient_phone"))
                .commentToAddressForClient((String) map.get("comment_to_address_for_client"))
                .garbageBags120Amount((Integer) map.get("garbage_bags_120_amount"))
                .boBags120Amount((Integer) map.get("bo_bags_120_amount"))
                .boBags20Amount((Integer) map.get("bo_bags_20_amount"))
                .totalSumOrder((Long) map.get("total_order_sum"))
                .certificateNumber((String) map.get("code"))
                .discount((Integer) map.get("points"))
                .amountDue((Long) map.get("amount_due"))
                .commentForOrderByClient((String) map.get("comment_for_order_by_client"))
                .payment((String) map.get("payment_system"))
                .dateOfExport((String) map.get("date_of_export"))
                .timeOfExport((String) map.get("time_of_export"))
                .idOrderFromShop((Long) map.get("id_order_from_shop"))
                .receivingStation((String) map.get("receiving_station"))
                .commentsForOrder((String) map.get("comments_for_order"))
                .build();
            for (Map<String, Object> objectMap : employees) {
                Long positionId = (Long) objectMap.get("position_id");
                if (positionId == 1) {
                    ourTable.setResponsibleManager((String) objectMap.get("name"));
                } else if (positionId == 2) {
                    ourTable.setResponsibleLogicMan((String) objectMap.get("name"));
                } else if (positionId == 3) {
                    ourTable.setResponsibleDriver((String) objectMap.get("name"));
                } else if (positionId == 4) {
                    ourTable.setResponsibleNavigator((String) objectMap.get("name"));
                }
            }
            result.add(new GetAllFieldsMainDto(ourTable));
        }
        return result;
    }
}
