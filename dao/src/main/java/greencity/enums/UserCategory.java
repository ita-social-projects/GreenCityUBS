package greencity.enums;

import lombok.Getter;

@Getter
public enum UserCategory {
    USERS_WITH_ORDERS_MADE_LESS_THAN_3_MONTHS(
                                              "Користувачі із замовленнями, зробленими менше 3 місяців тому.",
                                              "Users with orders made less than 3 months ago."),
    USERS_WITH_ORDERS_MADE_WITHIN_3_MONTHS_TO_1_YEAR(
                                                     "Користувачі із замовленнями,"
                                                         + " зробленими в межах від 3 місяців до 1 року.",
                                                     "Users with orders made within 3 months to 1 year."),
    USERS_WITH_ORDERS_MADE_MORE_THAN_1_YEAR("Користувачі із замовленнями, зробленими в більше року тому.",
                                            "Users with orders made more than 1 year ago."),
    USERS_WITHOUT_ORDERS("Користувачі які не зробили жодного замовлення.",
                         "Users without orders."),
    ALL_USERS("Всі користувачі.", "All users.");

    private final String description;
    private final String descriptionEng;

    UserCategory(String description, String descriptionEng) {
        this.description = description;
        this.descriptionEng = descriptionEng;
    }
}
