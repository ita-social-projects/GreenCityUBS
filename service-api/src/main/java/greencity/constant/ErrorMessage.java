package greencity.constant;

public final class ErrorMessage {
    public static final String CERTIFICATE_NOT_FOUND_BY_CODE = "Certificate does not exist by this code: ";
    public static final String CERTIFICATE_EXPIRED = "Certificate expired by this code: ";
    public static final String CERTIFICATE_STATUS = "Certificate has status 'EXPIRED' or 'USED'";
    public static final String CERTIFICATE_IS_USED = "The certificate has been used before or is not activated."
        + " Certificate code: ";
    public static final String CERTIFICATE_IS_NOT_ACTIVATED = "The certificate is not activated yet:";
    public static final String BAG_NOT_FOUND = "Bag does not exist by id: ";
    public static final String USER_DONT_HAVE_ENOUGH_POINTS = "User doesn't have enough bonus points.";
    public static final String TOO_MANY_CERTIFICATES = "Too many certificates was entered.";
    public static final String THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST =
        "The set of user data does not exist with id: ";
    public static final String INAVALID_DISTANCE_AMOUNT = "The distance should be between 0 and 20 km.";
    public static final String NO_SUCH_COORDINATES = "There are no any order with coordinates: ";
    public static final String INAVALID_LITRES_AMOUNT = "The amount of litres should be between 0 and 10.000 litres.";
    public static final String NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER = "Not found address with such id: ";
    public static final String UNDELIVERED_ORDERS_NOT_FOUND = "There are no any undelivered orders found.";
    public static final String PAYMENT_VALIDATION_ERROR = "The received payment data is not valid.";
    public static final String THE_USER_ALREADY_HAS_CONNECTED_TO_TELEGRAM_BOT =
        "The user already has connected to Telegram bot.";
    public static final String THE_USER_ALREADY_HAS_CONNECTED_TO_VIBER_BOT =
        "The user already has connected to Viber bot.";
    public static final String THE_MESSAGE_WAS_NOT_SENT = "The message was not sent.";
    public static final String USER_WITH_CURRENT_UUID_DOES_NOT_EXIST = "User with current uuid does not exist.";
    public static final String USER_WITH_CURRENT_ID_DOES_NOT_EXIST = "User with current id does not exist.";
    public static final String ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST = "Order with current id does not exist: ";
    public static final String RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST = "Recipient with current id does not exist.";
    public static final String THE_CHAT_ID_WAS_NOT_FOUND = "The chat id was not found.";
    public static final String NOT_FOUND_ADDRESS_BY_ORDER_ID = "Address not found for order by id: ";
    public static final String BAD_ORDER_STATUS_REQUEST = "Incorrect order status: ";
    public static final String ORDER_STATUS_NOT_FOUND = "Order status not found";
    public static final String ORDER_PAYMENT_STATUS_NOT_FOUND = "Order payment status not found";
    public static final String FILE_NOT_SAVED = "File hasn't been saved";
    public static final String EMPLOYEE_NOT_FOUND = "Employee with current id doesn't exist: ";
    public static final String EMPLOYEE_WITH_UUID_NOT_FOUND = "Employee with current uuid doesn't exist: ";
    public static final String CURRENT_PHONE_NUMBER_ALREADY_EXISTS = "Employee with this phone number already exists: ";
    public static final String CURRENT_EMAIL_ALREADY_EXISTS = "Employee with this email already exists: ";
    public static final String PHONE_NUMBER_PARSING_FAIL = "Phone number parsing fail: ";
    public static final String CURRENT_POSITION_ALREADY_EXISTS = "Position with this name already exists: ";
    public static final String POSITION_NOT_FOUND_BY_ID = "Position with current id doesn't exist: ";
    public static final String POSITION_NOT_FOUND = "Position doesn't exist";
    public static final String RECEIVING_STATION_ALREADY_EXISTS = "Receiving station already exists: ";
    public static final String RECEIVING_STATION_NOT_FOUND_BY_ID = "Receiving station with current id doesn't exist: ";
    public static final String RECEIVING_STATION_NOT_FOUND = "Receiving station doesn't exist.";
    public static final String EMPLOYEES_ASSIGNED_POSITION = "There are employees assigned to this position.";
    public static final String EMPLOYEE_WAS_NOT_SUCCESSFULLY_SAVED = "Employee was not successfully saved";
    public static final String PARSING_URL_FAILED = "Can't parse image's url: ";
    public static final String CANNOT_DELETE_DEFAULT_IMAGE = "You can't delete default image.";
    public static final String PAYMENT_NOT_FOUND = "Payment not found for order id: ";
    public static final String ADDRESS_ALREADY_EXISTS = "Address already exists";
    public static final String ADDRESS_NOT_FOUND = "Address not found";
    public static final String LOCATION_DOESNT_FOUND = "Location does not found";
    public static final String INTERRUPTED_EXCEPTION = "Interrupted exception thrown ";
    public static final String ORDER_ALREADY_HAS_VIOLATION = "Current order already has violation";
    public static final String ORDER_ALREADY_PAID = "Current order is already paid";
    public static final String VIOLATION_DOES_NOT_EXIST = "Violation does not exist for current order";
    public static final String ORDER_HAS_NOT_VIOLATION = "Order has not violation";
    public static final String EVENTS_NOT_FOUND_EXCEPTION = "Events didn't find in order id: ";
    public static final String NOT_ENOUGH_BIG_BAGS_EXCEPTION = "Not enough big bags, minimal amount is:";
    public static final String NOTIFICATION_DOES_NOT_EXIST = "Notification does not exist";
    public static final String NOTIFICATION_DOES_NOT_BELONG_TO_USER = "This notification does not belong to user";
    public static final String EMPLOYEE_ALREADY_ASSIGNED = "Manager already assigned with id: ";
    public static final String EMPLOYEE_DOESNT_EXIST = "Employee doesn't exist";
    public static final String EMPLOYEE_IS_NOT_ASSIGN =
        "Employee service could not be assign for order or this is manager which assign managers";
    public static final String SERVICE_IS_NOT_FOUND_BY_ID = "Couldn't found service with id: ";
    public static final String SERVICE_IS_NOT_FOUND_BY_TARIFF_ID = "Couldn't found service for tariff with id: ";
    public static final String SERVICE_ALREADY_EXISTS = "Service already exists for tariff with id: ";
    public static final String LOCATION_STATUS_IS_ALREADY_EXIST =
        "Current location already has status that's you wanna chose";
    public static final String COURIER_IS_NOT_FOUND_BY_ID = "Couldn't found courier by id: ";
    public static final String CANNOT_DEACTIVATE_COURIER = "Courier is already deactivated with id: ";
    public static final String BAG_WITH_THIS_STATUS_ALREADY_SET = "Bag with this status already set.";
    public static final String LIQPAY_PAYMENT_WITH_SELECTED_ID_NOT_FOUND =
        "Payment with selected id does not belong LiqPay.";
    public static final String ORDER_WITH_CURRENT_ID_NOT_FOUND = "Couldn't find order with id that you chose";
    public static final String TO_MUCH_BIG_BAG_EXCEPTION = "You choose to much big bag's max amount is: ";
    public static final String PRICE_OF_ORDER_GREATER_THAN_LIMIT =
        "The price of you're order without discount is greater than allowable limit: ";
    public static final String PRICE_OF_ORDER_LOWER_THAN_LIMIT =
        "The price of you're order without discount is lower than allowable limit: ";
    public static final String SOME_CERTIFICATES_ARE_INVALID =
        "SOME CERTIFICATES ARE INVALID. A valid certificate is listed here";
    public static final String CERTIFICATE_NOT_FOUND = "CERTIFICATE_NOT_FOUND";
    public static final String NOTIFICATION_TEMPLATE_NOT_FOUND = "Notification template doesn't exist";
    public static final String LOCATION_ALREADY_EXIST = " that you try to add was already created early";
    public static final String INCORRECT_ECO_NUMBER = "Incorrect format of Eco number";
    public static final String COURIER_ALREADY_EXISTS = "Courier with this name already exists";
    public static final String CANNOT_ACCESS_PAYMENT_STATUS = "Cannot access another user's payment status";
    public static final String USER_HAS_NO_OVERPAYMENT = "This user has no overpayment";
    public static final String CANNOT_DELETE_ADDRESS = "Cannot delete another user's address";
    public static final String CANNOT_ACCESS_PERSONAL_INFO = "Cannot access another user's personal info";
    public static final String CANNOT_ACCESS_ORDER_CANCELLATION_REASON =
        "Cannot access another user's order cancellation reason";
    public static final String USER_WITH_THIS_EMAIL_DOES_NOT_EXIST = "User with this email does not exist: ";
    public static final String LANGUAGE_ERROR = "Invalid language code";
    public static final String TARIFF_NOT_FOUND = "Couldn't found tariff with id: ";
    public static final String TARIFF_FOR_LOCATION_NOT_EXIST = "Could not find tariff for location with id: ";
    public static final String TARIFF_IS_ALREADY_EXISTS = "Tariff for such locations is already exists";
    public static final String USER_HAS_NOT_BEEN_DEACTIVATED = "User has not been deactivated";
    public static final String COULD_NOT_RETRIEVE_PASSWORD_STATUS = "Could not retrieve password status";
    public static final String COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE = "Could not retrieve checkout response";
    public static final String COULD_NOT_RETRIEVE_USER_DATA = "Could not retrieve user data";
    public static final String COULD_NOT_RETRIEVE_EMPLOYEE_AUTHORITY = "Could not retrieve employee's authority";
    public static final String EMPLOYEE_AUTHORITY_WAS_NOT_EDITED = "Employee's authority was not edited";
    public static final String EMPLOYEE_EMAIL_WAS_NOT_EDITED = "Employee's email was not edited";
    public static final String EMPLOYEE_AUTHORITIES_DONT_UPDATE = "Employee's authorities was not updated";
    public static final String COMMENT_ERROR = "Invalid comment= ";
    public static final String TOO_MUCH_POINTS_FOR_ORDER = "Too much points for order, maximum amount: ";
    public static final String TARIFF_FOR_ORDER_NOT_EXIST = "Could not find tariff for order with id: ";
    public static final String USE_ONLY_ENGLISH_LETTERS = "use only English letters";
    public static final String USE_ONLY_UKRAINIAN_LETTERS = "use only Ukrainian letters";
    public static final String CITY_NAME_CHARACTER_LIMIT = "A minimum of 3 to a maximum of 40 characters are allowed";
    public static final String CANNOT_ACCESS_ORDER_FOR_EMPLOYEE = "Cannot access order with id: ";
    public static final String NUMBER_OF_ADDRESSES_EXCEEDED = "Number of addresses reached maximum";
    public static final String BAGS_WITH_MIN_AMOUNT_OF_BIG_BAGS_NOT_FOUND =
        "There are not any tariff which include big bags for this location";
    public static final String TARIFF_LIMITS_ARE_INPUTTED_INCORRECTLY =
        "Limits are not inputted properly. You should only input AmountOfBags or PriceOfOrder.";
    public static final String MAX_BAG_VALUE_IS_INCORRECT = "Max bag value should be greater than min";
    public static final String MAX_PRICE_VALUE_IS_INCORRECT = "Max price value should be greater than min";

    public static final String MIN_MAX_VALUE_RESTRICTION = "Min and Max field must have different values";

    public static final String EMPLOYEE_WITH_CURRENT_UUID_WAS_NOT_DEACTIVATED = "Employee with current uuid was not "
        + "deactivated.";

    /**
     * Constructor.
     */

    private ErrorMessage() {
    }
}
