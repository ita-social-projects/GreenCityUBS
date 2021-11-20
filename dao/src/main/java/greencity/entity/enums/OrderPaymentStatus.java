package greencity.entity.enums;

import java.util.Optional;

public enum OrderPaymentStatus {
    PAID(1),
    UNPAID(2),
    HALF_PAID(3),
    PAYMENT_REFUNDED(4);

    private int statusValue;

    OrderPaymentStatus(final int statusValue) {
        this.statusValue = statusValue;
    }

    /**
     * This is method get status value.
     * 
     * @return {@link int}.
     */
    public int getStatusValue() {
        return statusValue;
    }

    /**
     * This is method which convert value from num to enum.
     *
     * @param value {@link Long}.
     * @return {@link Optional}.
     */
    public static Optional<String> getConvertedEnumFromLongToEnumAboutOrderPaymentStatus(Long value) {
        for (OrderPaymentStatus orderStatus : OrderPaymentStatus.values()) {
            if (orderStatus.getStatusValue() == value) {
                return Optional.ofNullable(orderStatus.toString());
            }
        }
        return Optional.empty();
    }
}
