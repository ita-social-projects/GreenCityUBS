package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationTrigger {
    ORDER_NOT_PAID_FOR_3_DAYS(
                              "Замовлення не оплачується протягом 3 днів після формування замовлення",
                              "The order is not paid 3 days after order was formed"),
    PAYMENT_SYSTEM_RESPONSE(
                            "Система отримує відповідь від платіжної системи",
                            "The system gets an answer from the payment system"),
    ORDER_ADDED_TO_ITINERARY_STATUS_CONFIRMED(
                                              "Менеджер включає замовлення в маршрут "
                                                  + "і змінює статус замовлення на «Підтверджено»",
                                              "The manager includes the order in the itinerary "
                                                  + "and changes order status to «Confirmed»"),
    STATUS_PARTIALLY_PAID(
                          "Зміна статусу платежу на «Частково оплачено»",
                          "Payment status changes to «Half paid»"),
    OVERPAYMENT_WHEN_STATUS_DONE(
                                 "Якщо в замовленні є переплата після зміни статусу замовлення на «Виконано»",
                                 "If the order has overpayment after changing order status to «Done»"),
    ORDER_VIOLATION_ADDED(
                          "Менеджер додає порушення до замовлення",
                          "Manager adds violation to order"),
    ORDER_VIOLATION_CANCELED(
                             "Керівник скасував статус припису про порушення",
                             "Manager canceled the violation order status"),
    ORDER_VIOLATION_CHANGED(
                            "Керівник змінив порушення в наказі",
                            "Manager changed the violation in the order"),
    TWO_MONTHS_AFTER_LAST_ORDER(
                                "2 місяці після останнього замовлення",
                                "2 months after last order"),
    ORDER_WAS_CANCELED(
                       "Повернення бонусів після скасування замовлення",
                       "Refund of bonuses after order cancellation"),
    CUSTOM(
           "Кастомна",
           "Custom"),
    ORDER_STATUS_CHANGED_FROM_FORMED_TO_BROUGHT_BY_HIMSELF(
                                                           "Статус замовлення змінений "
                                                               + "з «Сформовано» на «Привезе сам»",
                                                           "Order status changed "
                                                               + "from «Formed» to «Brought by himself»"),
    HALF_PAID_ORDER_STATUS_BROUGHT_BY_HIMSELF(
                                              "Статус не повністю оплаченого замовлення змінено на «Привезе сам»",
                                              "Status of half paid order changed to «Brought by himself»"),
    UNDERPAYMENT_WHEN_STATUS_DONE_OR_CANCELED(
                                              "Статус не оплаченого "
                                                  + "замовлення змінено на «Виконано» або «Скасовано»",
                                              "Status of unpaid order changed to «Done» or «Canceled»"),
    CREATED_NEW_ORDER(
                      "Створення нового замовлення",
                      "Created new order");

    private final String description;
    private final String descriptionEng;
}
