package greencity.entity.enums;

import java.util.Arrays;
import java.util.HashSet;

// if values changed, change in order_status_translations table is required
public enum OrderStatus {
    FORMED(1, OrderStatus.ADJUSTMENT, OrderStatus.CANCELLED, OrderStatus.BROUGHT_IT_HIMSELF),
    ADJUSTMENT(2, OrderStatus.BROUGHT_IT_HIMSELF, OrderStatus.CANCELLED, OrderStatus.CONFIRMED),
    BROUGHT_IT_HIMSELF(3, OrderStatus.DONE),
    CONFIRMED(4, OrderStatus.CANCELLED, OrderStatus.FORMED, OrderStatus.ON_THE_ROUTE),
    ON_THE_ROUTE(5, OrderStatus.DONE, OrderStatus.NOT_TAKEN_OUT),
    DONE(6, OrderStatus.DONE),
    NOT_TAKEN_OUT(7, OrderStatus.ADJUSTMENT, OrderStatus.NOT_TAKEN_OUT),
    CANCELLED(8, OrderStatus.CANCELLED);

    private int statusValue;
    private OrderStatus[] possibleStatuses;

    OrderStatus(final int value, OrderStatus... possibleStatuses) {
        this.statusValue = value;
        this.possibleStatuses = possibleStatuses;
    }

    /**
     * Method for getting a value of orderStatus, which is used for translating
     * order status wo different languages.
     *
     * @return {@link int} orderStatus value.
     */
    public int getNumValue() {
        return statusValue;
    }

    /**
     * Method for getting possible new status.
     *
     * @return {@link HashSet} possible statuses.
     */
    public HashSet<OrderStatus> possibleStatuses() {
        return new HashSet<>(Arrays.asList(possibleStatuses));
    }

     /**
     * This is method which convert value from num to enum.
     *
     * @param value {@link Long}.
     * @return {@link String}.
     */
    public static String getConvertedEnumFromLongToEnum(Long value) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.getNumValue() == value) {
                return orderStatus.toString();
            }
        }
        return "";
    }
}
