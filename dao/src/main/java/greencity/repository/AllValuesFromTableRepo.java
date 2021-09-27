package greencity.repository;

import greencity.entity.enums.SortingOrder;
import greencity.filters.SearchCriteria;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class AllValuesFromTableRepo {
    private static final String WHERE_ORDER_STATUS = "orders.order_status in (%s)";
    private static final String WHERE_PAYMENT_SYSTEM = "payment_system in (%s)";
    private static final String WHERE_RECEIVING_STATION = "receiving_station in (%s)";
    private static final String WHERE_DISTRICT = "district in (%s)";
    private static final String WHERE_ORDER_PAYMENT_STATUS = "order_payment_status in (%s)";
    private static final String WHERE_DATE_BETWEEN = "order_date::date between %s and %s";
    private static final String WHERE_DATE = "order_date::date = ?";

    private final JdbcTemplate jdbcTemplate;
    private static final String QUERY =
        "select distinct orders.id as orderId, orders.order_status , orders.order_date,\n"
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
            + "(select sum(payment.amount)\n"
            + "as total_Order_Sum from payment where payment.order_id = orders.id),\n"
            + "(select string_agg(certificate.code,',')\n"
            + "as order_certificate_code from certificate where order_id = orders.id),\n"
            + "(select string_agg(certificate.points::text,',')\n"
            + "as order_certificate_points from certificate where order_id = orders.id),\n"
            + "(payment.amount-certificate.points)as amount_Due,\n"
            + "orders.comment as comment_For_Order_By_Client,\n"
            + "payment.payment_system,\n"
            + "cast(orders.deliver_from as date) as date_Of_Export,\n"
            + "concat_Ws('-',cast(orders.deliver_from as time), cast(orders.deliver_to as time)) as time_Of_Export,\n"
            + "(select string_agg(payment.id::text,',')\n"
            + "as id_Order_From_Shop from payment where payment.order_id = orders.id) ,\n"
            + "orders.receiving_station,\n"
            + "orders.note as comments_for_order\n"
            + "from orders\n"
            + "left join ubs_user on orders.ubs_user_id = ubs_user.id\n"
            + "left join users on orders.users_id = users.id\n"
            + "left join address on ubs_user.address_id = address.id\n"
            + "left join payment on payment.order_id = orders.id\n"
            + "left join certificate on orders.id = certificate.order_id";

    private static final String EMPLOYEE_QUERY = "select concat_ws(' ', first_name, "
        + "last_name) as name,position_id from employees\n"
        + "left join employee_position on employees.id = employee_position.employee_id\n"
        + "left join order_employee on employees.id = order_employee.employee_id\n"
        + "where order_id = ";

    /**
     * Method returns all orders with additional information.
     */
    public List<Map<String, Object>> findAll(SearchCriteria searchCriteria, int pages, int size,
        String column, String sortingType) {
        int offset = pages * size;
        List<Object> preparedValues = new ArrayList<>();

        String subQuery = formSQLFilter(searchCriteria, column, sortingType, preparedValues);

        preparedValues.add(size);
        preparedValues.add(offset);

        return jdbcTemplate.queryForList(QUERY + subQuery, preparedValues.toArray());
    }

    /**
     * Method returns all employee for current order.
     */
    public List<Map<String, Object>> findAllEmployee(Long orderId) {
        return jdbcTemplate.queryForList(EMPLOYEE_QUERY + "?", orderId);
    }

    private String formSQLFilter(SearchCriteria searchCriteria, String column, String sortingType,
        List<Object> preparedValues) {
        String subQuery = "";

        if (searchCriteria.getOrderStatuses() != null
            || searchCriteria.getPaymentSystems() != null
            || searchCriteria.getOrderPaymentStatuses() != null
            || searchCriteria.getReceivingStations() != null
            || searchCriteria.getDateFrom() != null
            || searchCriteria.getDateTo() != null
            || searchCriteria.getOrderDate() != null
            || searchCriteria.getDistricts() != null) {
            subQuery += " where ";

            String sqlCondition;
            final String and = "and";
            boolean requireAnd = false;

            if (searchCriteria.getOrderStatuses() != null) {
                subQuery +=
                    formSubQueryAndPrepareValues(searchCriteria.getOrderStatuses(), preparedValues,
                        WHERE_ORDER_STATUS);
                requireAnd = true;
            }
            if (searchCriteria.getPaymentSystems() != null) {
                sqlCondition =
                    formSubQueryAndPrepareValues(searchCriteria.getPaymentSystems(), preparedValues,
                        WHERE_PAYMENT_SYSTEM);
                subQuery += requireAnd ? and + sqlCondition : sqlCondition;
            }
            if (searchCriteria.getReceivingStations() != null) {
                sqlCondition =
                    formSubQueryAndPrepareValues(searchCriteria.getPaymentSystems(), preparedValues,
                        WHERE_RECEIVING_STATION);
                subQuery += requireAnd ? and + sqlCondition : sqlCondition;
            }
            if (searchCriteria.getDistricts() != null) {
                sqlCondition =
                    formSubQueryAndPrepareValues(searchCriteria.getPaymentSystems(), preparedValues,
                        WHERE_DISTRICT);
                subQuery += requireAnd ? and + sqlCondition : sqlCondition;
            }
            if (searchCriteria.getOrderPaymentStatuses() != null) {
                sqlCondition =
                    formSubQueryAndPrepareValues(searchCriteria.getOrderPaymentStatuses(), preparedValues,
                        WHERE_ORDER_PAYMENT_STATUS);
                subQuery += requireAnd ? and + sqlCondition : sqlCondition;
            }
            if ((searchCriteria.getDateFrom() != null && searchCriteria.getDateTo() != null)
                && searchCriteria.getOrderDate() == null) {
                sqlCondition =
                    String.format(WHERE_DATE_BETWEEN, "?", "?");

                preparedValues.add(Date.valueOf(searchCriteria.getDateFrom()));
                preparedValues.add(Date.valueOf(searchCriteria.getDateTo()));

                subQuery += requireAnd ? and + sqlCondition : sqlCondition;
            }
            if (searchCriteria.getOrderDate() != null
                && (searchCriteria.getDateFrom() == null && searchCriteria.getDateTo() == null)) {
                subQuery += requireAnd ? and + WHERE_DATE : WHERE_DATE;
                preparedValues.add(Date.valueOf(searchCriteria.getOrderDate()));
            }
        }

        return addSortingAndOrderToSubQuery(subQuery, column, sortingType);
    }

    private String formSubQueryAndPrepareValues(Object[] array, List<Object> preparedValues, String whereClause) {
        String args = Arrays.stream(array)
            .map(s -> "?")
            .collect(Collectors.joining(", "));

        Arrays.stream(array)
            .map(Object::toString)
            .forEach(preparedValues::add);

        return String.format(whereClause, args);
    }

    private String addSortingAndOrderToSubQuery(String subQuery, String column, String sortingType) {
        if (!sortingType.equalsIgnoreCase(SortingOrder.ASC.name())
            && !sortingType.equalsIgnoreCase(SortingOrder.DESC.name())) {
            throw new IllegalArgumentException("Invalid sorting type");
        }

        StringBuilder query = new StringBuilder();

        query.append(subQuery).append(" order by ").append(column)
            .append(" ").append(sortingType).append(" limit ? offset ?");

        subQuery = String.valueOf(query);
        return subQuery;
    }
}
