package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BonusReason {
    RETURN_OVERPAY("Повернення переплати за замовлення", "Refund of overpayment for the order"),
    REFUND_CANCELED_ORDER("Зарахування оплати скасованого замовлення", "Enrollment of payment for canceled order"),
    DEBIT_PAYMENT("Списання у рахунок оплати замовлення", "Write-off of the payment of the order"),
    RETURN_UNPAID_ORDER("Повернення з неуспішної оплати", "Refund from unsuccessful payment");

    private final String descriptionUk;
    private final String descriptionEn;
}
