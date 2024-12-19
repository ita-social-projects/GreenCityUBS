package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BonusReasonTest {
    @Test
    void returnOverpayConstantValidTest() {
        BonusReason reason = BonusReason.RETURN_OVERPAY;

        assertEquals("Повернення переплати за замовлення", reason.getDescriptionUa());
        assertEquals("Refund of overpayment for the order", reason.getDescriptionEn());
    }

    @Test
    void refundCanceledOrderConstantValidTest() {
        BonusReason reason = BonusReason.REFUND_CANCELED_ORDER;

        assertEquals("Зарахування оплати скасованого замовлення", reason.getDescriptionUa());
        assertEquals("Enrollment of payment for canceled order", reason.getDescriptionEn());
    }

    @Test
    void debitPaymentConstantValidTest() {
        BonusReason reason = BonusReason.DEBIT_PAYMENT;

        assertEquals("Списання у рахунок оплати замовлення", reason.getDescriptionUa());
        assertEquals("Write-off of the payment of the order", reason.getDescriptionEn());
    }
}
