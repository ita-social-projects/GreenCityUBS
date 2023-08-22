package greencity.enums;

import java.util.Arrays;

public enum OrderStatus {
    FORMED(1, "ADJUSTMENT", "BROUGHT_IT_HIMSELF", OrderStatus.CANCELED_STR),
    ADJUSTMENT(2, "FORMED", "BROUGHT_IT_HIMSELF", "CONFIRMED", OrderStatus.CANCELED_STR),
    BROUGHT_IT_HIMSELF(3, "DONE", OrderStatus.CANCELED_STR),
    CONFIRMED(4, "FORMED", "ON_THE_ROUTE", "BROUGHT_IT_HIMSELF", OrderStatus.CANCELED_STR),
    ON_THE_ROUTE(5, "DONE", "NOT_TAKEN_OUT", OrderStatus.CANCELED_STR),
    DONE(6, "DONE"),
    NOT_TAKEN_OUT(7, "ADJUSTMENT", "BROUGHT_IT_HIMSELF", OrderStatus.CANCELED_STR),
    CANCELED(8, OrderStatus.CANCELED_STR);

    private static final String CANCELED_STR = "CANCELED";
    private final int statusValue;
    private final String[] possibleStatus;

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
