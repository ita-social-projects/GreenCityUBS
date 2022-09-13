package greencity.entity.enums;

import java.util.Arrays;

// if values changed, change in order_status_translations table is required
public enum OrderStatus {
    FORMED(1, "ADJUSTMENT", OrderStatus.CANCELED_STR, "BROUGHT_IT_HIMSELF"),
    ADJUSTMENT(2, "BROUGHT_IT_HIMSELF", OrderStatus.CANCELED_STR, "CONFIRMED"),
    BROUGHT_IT_HIMSELF(3, "DONE"),
    CONFIRMED(4, OrderStatus.CANCELED_STR, "FORMED", "ON_THE_ROUTE"),
    ON_THE_ROUTE(5, "DONE", "NOT_TAKEN_OUT"),
    DONE(6, "DONE"),
    NOT_TAKEN_OUT(7, "ADJUSTMENT", "NOT_TAKEN_OUT"),
    CANCELED(8, OrderStatus.CANCELED_STR),
    PAID(9, "PAID");

    private static final String CANCELED_STR = "CANCELED";
    private int statusValue;
    private String[] possibleStatus;

    OrderStatus(final int value, String... possibleStatus) {
        this.statusValue = value;
        this.possibleStatus = possibleStatus;
    }

    /**
     * Method for getting a value of orderStatus, which is used for translating
     * order status to different languages.
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
     * Method for checking if desired status is possible to use with current status.
     * 
     * @param desiredStatus {@link String}.
     * @return boolean.
     */
    public boolean checkPossibleStatus(String desiredStatus) {
        return Arrays.asList(possibleStatus).contains(desiredStatus);
    }
}
