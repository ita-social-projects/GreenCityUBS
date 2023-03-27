package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationTime {
    IMMEDIATELY(
            "Одразу",
            "Immediately"
    ),
    TWO_MONTHS_AFTER_LAST_ORDER(
            "Система щодня перевіряє BD і надсилає повідомлення, якщо замовлення було зроблено 2 місяці тому",
            "System checks BD daily and sends messages in case the order was made 2 months ago"
    ),
    AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID(
            "Система перевіряє BD щодня о 18.00 і відправляє повідомлення, якщо замовлення було сформовано 3 дні тому і не було оплачено клієнтом.",
            "System checks BD at 18.00 daily and sends messages in case the order was formed 3 days ago and wasn’t paid by the client."
    );

    private final String description;
    private final String descriptionEng;
}
