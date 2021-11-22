package greencity.entity.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// if values changed, change in order_status_translations table is required
public enum OrderStatus {
    FORMED(1, "CANCELED"),
    ADJUSTMENT(2, "CANCELED"),
    BROUGHT_IT_HIMSELF(3, "CANCELED"),
    CONFIRMED(4, "CANCELED"),
    ON_THE_ROUTE(5, "CANCELED"),
    DONE(6, "CANCELED"),
    NOT_TAKEN_OUT(7, "CANCELED"),
    CANCELED(8, "CANCELED");

    private int statusValue;
    private String[] possibleStatus;

    OrderStatus(final int value, String... possibleStatus) {
        this.statusValue = value;
        this.possibleStatus = possibleStatus;
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

    /**
     * Method for.
     *
     * @return {@link HashSet} orderStatuses.
     */
    public Set<String> getPossibleStatuses() {
        return new HashSet<>(Arrays.asList(possibleStatus));
    }
}
