package greencity.entity.enums;

import java.util.Arrays;
import java.util.HashSet;

// if values changed, change in order_status_translations table is required
public enum OrderStatus {
    FORMED(1, "ADJUSTMENT", "CANCELLED", "BROUGHT_IT_HIMSELF"),
    ADJUSTMENT(2, "BROUGHT_IT_HIMSELF", "CANCELLED", "CONFIRMED"),
    BROUGHT_IT_HIMSELF(3, "DONE"),
    CONFIRMED(4, "CANCELLED", "FORMED", "ON_THE_ROUTE"),
    ON_THE_ROUTE(5, "DONE", "NOT_TAKEN_OUT"),
    DONE(6, "DONE"),
    NOT_TAKEN_OUT(7, "ADJUSTMENT", "NOT_TAKEN_OUT"),
    CANCELLED(8, "CANCELLED");

    private int statusValue;
    private String[] possibleStatuses;

    OrderStatus(final int value, String... possibleStatuses) {
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
    public HashSet<String> possibleStatuses() {
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
