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

    private int statusValue;

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

    public static OrderStatus changeStatusByRules(OrderStatus newStatus, OrderStatus current) {
        switch (current) {
            case FORMED:
                switch (newStatus) {
                    case ADJUSTMENT:
                        return ADJUSTMENT;
                    case CANCELLED:
                        return CANCELLED;
                    case BROUGHT_IT_HIMSELF:
                        return BROUGHT_IT_HIMSELF;
                    default:
                        return current;
                }
            case ADJUSTMENT:
                switch (newStatus) {
                    case BROUGHT_IT_HIMSELF:
                        return BROUGHT_IT_HIMSELF;
                    case CANCELLED:
                        return CANCELLED;
                    case CONFIRMED:
                        return CONFIRMED;
                    default:
                        return current;
                }
            case BROUGHT_IT_HIMSELF:
                switch (newStatus) {
                    case DONE:
                        return DONE;
                    default:
                        return current;
                }
            case CONFIRMED:
                switch (newStatus) {
                    case CANCELLED:
                        return CANCELLED;
                    case FORMED:
                        return FORMED;
                    case ON_THE_ROUTE:
                        return ON_THE_ROUTE;
                    default:
                        return current;
                }
            case ON_THE_ROUTE:
                switch (newStatus) {
                    case DONE:
                        return DONE;
                    case NOT_TAKEN_OUT:
                        return NOT_TAKEN_OUT;
                    default:
                        return current;
                }
            case NOT_TAKEN_OUT:
                switch (newStatus) {
                    case ADJUSTMENT:
                        return ADJUSTMENT;
                    case CANCELLED:
                        return CANCELLED;
                    default:
                        return current;
                }
            case CANCELLED:
                return current;
            case DONE:
                return current;
            default:
                return current;
        }
    }
}
