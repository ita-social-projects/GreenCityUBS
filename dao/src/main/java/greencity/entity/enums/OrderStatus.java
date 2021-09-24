package greencity.entity.enums;

// if values changed, change in order_status_translations table is required
public enum OrderStatus {
    FORMED(1),
    ADJUSTMENT(2),
    BROUGHT_IT_HIMSELF(3),
    CONFIRMED(4),
    ON_THE_ROUTE(5),
    DONE(6),
    NOT_TAKEN_OUT(7),
    CANCELLED(8);

    private final int statusValue;

    OrderStatus(final int value) {
        this.statusValue = value;
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
}
