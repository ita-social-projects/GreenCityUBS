package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTriggerTest {

    @Test
    void testOrderNotPaidFor3Days() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS;
        assertEquals("Замовлення не оплачується протягом 3 днів після формування замовлення", trigger.getDescriptionUk());
        assertEquals("The order is not paid 3 days after order was formed", trigger.getDescriptionEn());
    }

    @Test
    void testPaymentSystemResponse() {
        NotificationTrigger trigger = NotificationTrigger.PAYMENT_SYSTEM_RESPONSE;
        assertEquals("Система отримує відповідь від платіжної системи", trigger.getDescriptionUk());
        assertEquals("The system gets an answer from the payment system", trigger.getDescriptionEn());
    }

    @Test
    void testOrderAddedToItineraryStatusConfirmed() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_ADDED_TO_ITINERARY_STATUS_CONFIRMED;
        assertEquals("Менеджер включає замовлення в маршрут і змінює статус замовлення на «Підтверджено»",
                trigger.getDescriptionUk());
        assertEquals("The manager includes the order in the itinerary and changes order status to «Confirmed»",
                trigger.getDescriptionEn());
    }

    @Test
    void testStatusPartiallyPaid() {
        NotificationTrigger trigger = NotificationTrigger.STATUS_PARTIALLY_PAID;
        assertEquals("Зміна статусу платежу на «Частково оплачено»", trigger.getDescriptionUk());
        assertEquals("Payment status changes to «Half paid»", trigger.getDescriptionEn());
    }

    @Test
    void testOverpaymentWhenStatusDone() {
        NotificationTrigger trigger = NotificationTrigger.OVERPAYMENT_WHEN_STATUS_DONE;
        assertEquals("Якщо в замовленні є переплата після зміни статусу замовлення на «Виконано»",
                trigger.getDescriptionUk());
        assertEquals("If the order has overpayment after changing order status to «Done»", trigger.getDescriptionEn());
    }

    @Test
    void testOrderViolationAdded() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_VIOLATION_ADDED;
        assertEquals("Менеджер додає порушення до замовлення", trigger.getDescriptionUk());
        assertEquals("Manager adds violation to order", trigger.getDescriptionEn());
    }

    @Test
    void testOrderViolationCanceled() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_VIOLATION_CANCELED;
        assertEquals("Керівник скасував статус припису про порушення", trigger.getDescriptionUk());
        assertEquals("Manager canceled the violation order status", trigger.getDescriptionEn());
    }

    @Test
    void testOrderViolationChanged() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_VIOLATION_CHANGED;
        assertEquals("Керівник змінив порушення в наказі", trigger.getDescriptionUk());
        assertEquals("Manager changed the violation in the order", trigger.getDescriptionEn());
    }

    @Test
    void testTwoMonthsAfterLastOrder() {
        NotificationTrigger trigger = NotificationTrigger.TWO_MONTHS_AFTER_LAST_ORDER;
        assertEquals("2 місяці після останнього замовлення", trigger.getDescriptionUk());
        assertEquals("2 months after last order", trigger.getDescriptionEn());
    }

    @Test
    void testOrderWasCanceled() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_WAS_CANCELED;
        assertEquals("Повернення бонусів після скасування замовлення", trigger.getDescriptionUk());
        assertEquals("Refund of bonuses after order cancellation", trigger.getDescriptionEn());
    }

    @Test
    void testCustom() {
        NotificationTrigger trigger = NotificationTrigger.CUSTOM;
        assertEquals("Кастомна", trigger.getDescriptionUk());
        assertEquals("Custom", trigger.getDescriptionEn());
    }

    @Test
    void testOrderStatusChangedFromFormedToBroughtByHimself() {
        NotificationTrigger trigger = NotificationTrigger.ORDER_STATUS_CHANGED_FROM_FORMED_TO_BROUGHT_BY_HIMSELF;
        assertEquals("Статус замовлення змінений з «Сформовано» на «Привезе сам»", trigger.getDescriptionUk());
        assertEquals("Order status changed from «Formed» to «Brought by himself»", trigger.getDescriptionEn());
    }

    @Test
    void testHalfPaidOrderStatusBroughtByHimself() {
        NotificationTrigger trigger = NotificationTrigger.HALF_PAID_ORDER_STATUS_BROUGHT_BY_HIMSELF;
        assertEquals("Статус не повністю оплаченого замовлення змінено на «Привезе сам»", trigger.getDescriptionUk());
        assertEquals("Status of half paid order changed to «Brought by himself»", trigger.getDescriptionEn());
    }

    @Test
    void testUnderpaymentWhenStatusDoneOrCanceled() {
        NotificationTrigger trigger = NotificationTrigger.UNDERPAYMENT_WHEN_STATUS_DONE_OR_CANCELED;
        assertEquals("Статус не оплаченого замовлення змінено на «Виконано» або «Скасовано»", trigger.getDescriptionUk());
        assertEquals("Status of unpaid order changed to «Done» or «Canceled»", trigger.getDescriptionEn());
    }

    @Test
    void testTariffPriceWasChanged() {
        NotificationTrigger trigger = NotificationTrigger.TARIFF_PRICE_WAS_CHANGED;
        assertEquals("Зміна вартості тарифу", trigger.getDescriptionUk());
        assertEquals("Change in the price of the tariff", trigger.getDescriptionEn());
    }

    @Test
    void testCreatedNewOrder() {
        NotificationTrigger trigger = NotificationTrigger.CREATED_NEW_ORDER;
        assertEquals("Створення нового замовлення", trigger.getDescriptionUk());
        assertEquals("Create a new order", trigger.getDescriptionEn());
    }
}
