package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum PaymentStatus {
    PAID(1),
    UNPAID(2),
    HALF_PAID(3),
    PAYMENT_REFUNDED(4);

    private int value;

    /**
     * Method for getting a value of paymentStatus, which is used for translating
     * order status to different languages.
     *
     * @return {@link int} paymentStatus value.
     */
    public int getNumValue() {
        return value;
    }
}
