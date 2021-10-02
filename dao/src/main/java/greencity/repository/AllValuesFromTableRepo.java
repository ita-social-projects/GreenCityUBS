package greencity.repository;

import greencity.entity.enums.SortingOrder;
import greencity.filters.SearchCriteria;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.*;
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
        "select distinct (orders.id) as order_id, \n"
            + "orders.order_status as order_status, \n"
            + "orders.order_payment_status as payment_status,\n"
            + "orders.order_date as order_date, payment.order_time as payment_date,\n"
            + "concat_ws(' ', users.recipient_name, users.recipient_surname) as client_name,\n"
            + "users.recipient_phone as phone_number, users.recipient_email as email,\n"
            + "concat_ws(' ', ubs_user.first_name, ubs_user.last_name) as sender_name,\n"
            + "ubs_user.phone_number as sender_phone, ubs_user.email as sender_email,\n"
            + "users.violations, address.district,\n"
            + "concat_ws(', ', address.street, address.house_number,\n"
            + "address.house_corpus, address.entrance_number) as address,\n"
            + "address.comment as comment_to_address_for_client,\n"
            + "(select sum(order_bag_mapping.amount) as bags_amount\n"
            + "from order_bag_mapping where order_bag_mapping.order_id = orders.id),\n"
            + "(select sum(payment.amount) as total_order_sum\n"
            + "from payment where payment.order_id = orders.id),\n"
            + "(select string_agg(certificate.code,', ') as order_certificate_code \n"
            + "from certificate where order_id = orders.id),\n"
            + "(select string_agg(certificate.points::text,', ') as order_certificate_points \n"
            + "from certificate where order_id = orders.id),\n"
            + "(payment.amount-certificate.points) as amount_due,\n"
            + "orders.comment as comment_for_order_by_client,\n"
            + "cast(orders.deliver_from as date) as date_of_export,\n"
            + "concat_ws('-', cast(orders.deliver_from as time), \n"
            + "cast(orders.deliver_to as time)) as time_of_export,\n"
            + "(select string_agg(payment.id::text,', ') as id_order_from_shop \n"
            + "from payment where payment.order_id = orders.id), \n"
            + "orders.receiving_station as receiving_station,\n"
            + "orders.note as comments_for_order\n"
            + "from orders\n"
            + "left join ubs_user on orders.ubs_user_id = ubs_user.id\n"
            + "left join users on orders.users_id = users.id\n"
            + "left join address on ubs_user.address_id = address.id\n"
            + "left join payment on payment.order_id = orders.id\n"
            + "left join certificate on certificate.order_id = orders.id";

    private static final String SEARCH = "lower (concat(orders.id, orders.order_status, orders.order_date, \n"
        + "ubs_user.first_name, ubs_user.last_name, ubs_user.phone_number, \n"
        + "ubs_user.email, users.violations, address.district, address.city, \n"
        + "address.street, address.house_number,address.house_corpus, \n"
        + "address.entrance_number, users.recipient_name, users.recipient_email, \n"
        + "users.recipient_phone, address.comment, payment.amount,\n"
        + "certificate.code, certificate.points::text, payment.amount-certificate.points, \n"
        + "orders.comment, payment.payment_system, orders.deliver_from,\n"
        + "orders.deliver_to, payment.id::text, orders.receiving_station, orders.note)) "
        + "like lower(concat( '%',?,'%'))";

    private static final String EMPLOYEE_QUERY = "select concat_ws(' ', first_name, "
        + "last_name) as name,position_id from employees\n"
        + "left join employee_position on employees.id = employee_position.employee_id\n"
        + "left join order_employee on employees.id = order_employee.employee_id\n"
        + "where order_id = ";

    /**
     * Method returns all orders with additional information.
     */
    public List<Map<String, Object>> findAll(SearchCriteria searchCriteria, int page, int size,
        String column, SortingOrder sortingType) {
        int offset = page * size;
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

    private String formSQLFilter(SearchCriteria searchCriteria, String column, SortingOrder sortingOrder,
        List<Object> preparedValues) {
        String subQuery = "";
        boolean requireAnd = false;
        final String and = " and ";

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
        if (searchCriteria.getSearchValue() != null) {
            subQuery += requireAnd ? and + SEARCH : " where " + SEARCH;
            preparedValues.add(searchCriteria.getSearchValue());
        }

        subQuery += String.format(" order by %s %s limit ? offset ?", column, sortingOrder);

        return subQuery;
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

    /**
     * Method returns all columns from our custom table.
     */
    public List<String> getCustomColumns() {
        List<String> columns = new ArrayList<>();

        columns.add("order_id");
        columns.add("order_status");
        columns.add("payment_status");
        columns.add("order_date");
        columns.add("payment_date");
        columns.add("client_name");
        columns.add("phone_number");
        columns.add("email");
        columns.add("sender_name");
        columns.add("sender_phone");
        columns.add("sender_email");
        columns.add("violations");
        columns.add("location");
        columns.add("district");
        columns.add("address");
        columns.add("comment_to_address_for_client");
        columns.add("bags_amount");
        columns.add("total_order_sum");
        columns.add("order_certificate_code");
        columns.add("order_certificate_points");
        columns.add("amount_due");
        columns.add("comment_for_order_by_client");
        columns.add("payment");
        columns.add("date_of_export");
        columns.add("time_of_export");
        columns.add("id_order_from_shop");
        columns.add("receiving_station");
        columns.add("responsible_manager");
        columns.add("responsible_logic_man");
        columns.add("responsible_driver");
        columns.add("responsible_navigator");
        columns.add("comments_for_order");

        return columns;
    }
}
