package greencity.enums;

import java.util.Arrays;

public enum OrderStatus {
    FORMED(1, OrderStatus.ADJUSTMENT_STR, OrderStatus.BROUGHT_IT_HIMSELF_STR, OrderStatus.CANCELED_STR),
    ADJUSTMENT(2, OrderStatus.FORMED_STR, OrderStatus.BROUGHT_IT_HIMSELF_STR, OrderStatus.CONFIRMED_STR,
               OrderStatus.CANCELED_STR),
    BROUGHT_IT_HIMSELF(3, OrderStatus.DONE_STR, OrderStatus.CANCELED_STR),
    CONFIRMED(4, OrderStatus.FORMED_STR, OrderStatus.ON_THE_ROUTE_STR, OrderStatus.BROUGHT_IT_HIMSELF_STR,
              OrderStatus.CANCELED_STR),
    ON_THE_ROUTE(5, OrderStatus.DONE_STR, OrderStatus.NOT_TAKEN_OUT_STR, OrderStatus.CANCELED_STR),
    DONE(6, OrderStatus.DONE_STR),
    NOT_TAKEN_OUT(7, OrderStatus.ADJUSTMENT_STR, OrderStatus.BROUGHT_IT_HIMSELF_STR, OrderStatus.CANCELED_STR),
    CANCELED(8, OrderStatus.CANCELED_STR);

    private static final String CANCELED_STR = "CANCELED";
    private static final String BROUGHT_IT_HIMSELF_STR = "BROUGHT_IT_HIMSELF";
    private static final String ADJUSTMENT_STR = "ADJUSTMENT";
    private static final String DONE_STR = "DONE";
    private static final String FORMED_STR = "FORMED";
    private static final String CONFIRMED_STR = "CONFIRMED";
    private static final String ON_THE_ROUTE_STR = "ON_THE_ROUTE";
    private static final String NOT_TAKEN_OUT_STR = "NOT_TAKEN_OUT";
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
