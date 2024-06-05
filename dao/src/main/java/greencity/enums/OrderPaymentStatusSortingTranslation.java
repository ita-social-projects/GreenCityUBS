package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum OrderPaymentStatusSortingTranslation implements SortingTranslation<OrderPaymentStatusSortingTranslation> {
    UNPAID(1),
    PAYMENT_REFUNDED(2),
    PAID(3),
    HALF_PAID(4),
    OTHER(5);

    private final int sortOrder;

    private static final Set<OrderPaymentStatusSortingTranslation> ASC_ORDER_PAYMENT_STATUS_TRANSLATIONS =
        Collections.unmodifiableSet(EnumSet.allOf(OrderPaymentStatusSortingTranslation.class));

    /**
     * Method returns order payment status translations sorted in ascending order
     * according to the Ukrainian alphabet.
     *
     * @return {@link Set} of {@link OrderPaymentStatusSortingTranslation}
     */
    @Override
    public Set<OrderPaymentStatusSortingTranslation> getSortedTranslations() {
        return ASC_ORDER_PAYMENT_STATUS_TRANSLATIONS;
    }

    @Override
    public OrderPaymentStatusSortingTranslation getOtherStatus() {
        return OTHER;
    }
}
