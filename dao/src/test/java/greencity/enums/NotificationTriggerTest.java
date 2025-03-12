package greencity.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTriggerTest {

    @Test
    void testOrderNotPaidFor3Days() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS;
        assertEquals("Замовлення не оплачується протягом 3 днів після формування замовлення", trigger.getDescription());
        assertEquals("The order is not paid 3 days after order was formed", trigger.getDescriptionEng());
    }

    @Test
    void testPaymentSystemResponse() {
        NotificationTrigger trigger = NotificationTrigger.PAYMENT_SYSTEM_RESPONSE;
        assertEquals("Система отримує відповідь від платіжної системи", trigger.getDescription());
        assertEquals("The system gets an answer from the payment system", trigger.getDescriptionEng());
    }

    @Test
    void testOrderAddedToItineraryStatusConfirmed() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_ADDED_TO_ITINERARY_STATUS_CONFIRMED;
        assertEquals("Менеджер включає замовлення в маршрут і змінює статус замовлення на «Підтверджено»",
            trigger.getDescription());
        assertEquals("The manager includes the order in the itinerary and changes order status to «Confirmed»",
            trigger.getDescriptionEng());
    }

    @Test
    void testStatusPartiallyPaid() {
        NotificationTrigger trigger = NotificationTrigger.STATUS_PARTIALLY_PAID;
        assertEquals("Зміна статусу платежу на «Частково оплачено»", trigger.getDescription());
        assertEquals("Payment status changes to «Half paid»", trigger.getDescriptionEng());
    }

    @Test
    void testOverpaymentWhenStatusDone() {
        NotificationTrigger trigger = NotificationTrigger.OVERPAYMENT_WHEN_STATUS_DONE;
        assertEquals("Якщо в замовленні є переплата після зміни статусу замовлення на «Виконано»",
            trigger.getDescription());
        assertEquals("If the order has overpayment after changing order status to «Done»", trigger.getDescriptionEng());
    }

    @Test
    void testOrderViolationAdded() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_VIOLATION_ADDED;
        assertEquals("Менеджер додає порушення до замовлення", trigger.getDescription());
        assertEquals("Manager adds violation to order", trigger.getDescriptionEng());
    }

    @Test
    void testOrderViolationCanceled() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_VIOLATION_CANCELED;
        assertEquals("Керівник скасував статус припису про порушення", trigger.getDescription());
        assertEquals("Manager canceled the violation order status", trigger.getDescriptionEng());
    }

    @Test
    void testOrderViolationChanged() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_VIOLATION_CHANGED;
        assertEquals("Керівник змінив порушення в наказі", trigger.getDescription());
        assertEquals("Manager changed the violation in the order", trigger.getDescriptionEng());
    }

    @Test
    void testTwoMonthsAfterLastOrder() {
        NotificationTrigger trigger = NotificationTrigger.TWO_MONTHS_AFTER_LAST_ORDER;
        assertEquals("2 місяці після останнього замовлення", trigger.getDescription());
        assertEquals("2 months after last order", trigger.getDescriptionEng());
    }

    @Test
    void testOrderWasCanceled() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_WAS_CANCELED;
        assertEquals("Повернення бонусів після скасування замовлення", trigger.getDescription());
        assertEquals("Refund of bonuses after order cancellation", trigger.getDescriptionEng());
    }

    @Test
    void testCustom() {
        NotificationTrigger trigger = NotificationTrigger.CUSTOM;
        assertEquals("Кастомна", trigger.getDescription());
        assertEquals("Custom", trigger.getDescriptionEng());
    }

    @Test
    void testOrderStatusChangedFromFormedToBroughtByHimself() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_STATUS_CHANGED_FROM_FORMED_TO_BROUGHT_BY_HIMSELF;
        assertEquals("Статус замовлення змінений з «Сформовано» на «Привезе сам»", trigger.getDescription());
        assertEquals("Order status changed from «Formed» to «Brought by himself»", trigger.getDescriptionEng());
    }

    @Test
    void testHalfPaidOrderStatusBroughtByHimself() {
        NotificationTrigger trigger = NotificationTrigger.HALF_PAID_ORDER_STATUS_BROUGHT_BY_HIMSELF;
        assertEquals("Статус не повністю оплаченого замовлення змінено на «Привезе сам»", trigger.getDescription());
        assertEquals("Status of half paid order changed to «Brought by himself»", trigger.getDescriptionEng());
    }

    @Test
    void testUnderpaymentWhenStatusDoneOrCanceled() {
        NotificationTrigger trigger = NotificationTrigger.UNDERPAYMENT_WHEN_STATUS_DONE_OR_CANCELED;
        assertEquals("Статус не оплаченого замовлення змінено на «Виконано» або «Скасовано»", trigger.getDescription());
        assertEquals("Status of unpaid order changed to «Done» or «Canceled»", trigger.getDescriptionEng());
    }

    @Test
    void testTariffPriceWasChanged() {
        NotificationTrigger trigger = NotificationTrigger.TARIFF_PRICE_WAS_CHANGED;
        assertEquals("Зміна вартості тарифу", trigger.getDescription());
        assertEquals("Change in the price of the tariff", trigger.getDescriptionEng());
    }

    @Test
    void testCreatedNewOrder() {
        NotificationTrigger trigger = NotificationTrigger.CREATED_NEW_ORDER;
        assertEquals("Створення нового замовлення", trigger.getDescription());
        assertEquals("Create a new order", trigger.getDescriptionEng());
    }
}
