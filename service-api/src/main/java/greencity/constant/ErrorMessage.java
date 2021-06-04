package greencity.constant;

public class ErrorMessage {
    public static String CERTIFICATE_NOT_FOUND_BY_CODE = "Certificate does not exist by this code: ";
    public static String CERTIFICATE_EXPIRED = "Certificate expired by this code: ";
    public static String CERTIFICATE_IS_USED = "The certificate has been used before or is not activated."
        + " Certificate code: ";
    public static String CERTIFICATE_IS_NOT_ACTIVATED = "The certificate is not activated yet:";
    public static String BAG_NOT_FOUND = "Bag does not exist by id: ";
    public static String USER_DONT_HAVE_ENOUGH_POINTS = "User doesn't have enough bonus points.";
    public static String AMOUNT_OF_POINTS_BIGGER_THAN_SUM = "Amount of bonus points to use is bigger than order sum.";
    public static String TOO_MANY_CERTIFICATES = "Too many certificates was entered.";
    public static String SUM_IS_COVERED_BY_CERTIFICATES = "Bonus points shouldn't be used if sum to pay is "
        + "covered by certificates.";
    public static String THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST = "The set of user data does not exist with id: ";
    public static String INAVALID_DISTANCE_AMOUNT = "The distance should be between 0 and 20 km.";
    public static String NO_SUCH_COORDINATES = "There are no any order with coordinates: ";
    public static String INAVALID_LITRES_AMOUNT = "The amount of litres should be between 0 and 10.000 litres.";
    public static String NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER = "Not found address with such id: ";
    public static String UNDELIVERED_ORDERS_NOT_FOUND = "There are no any undelivered orders found.";
    public static String MINIMAL_SUM_VIOLATION = "The minimal order sum should be 500 UAH.";
    public static String PAYMENT_VALIDATION_ERROR = "The received payment data is not valid.";
    public static String THE_USER_ALREADY_HAS_CONNECTED_TO_TELEGRAM_BOT =
        "The user already has connected to Telegram bot.";
    public static String THE_USER_ALREADY_HAS_CONNECTED_TO_VIBER_BOT =
        "The user already has connected to Viber bot.";
    public static String THE_MESSAGE_WAS_NOT_SEND = "The message was not send.";
    public static String USER_WITH_CURRENT_UUID_DOES_NOT_EXIST = "User with current uuid does mot exist.";
    public static String ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST = "Order with current id does mot exist.";
    public static String THE_CHAT_ID_WAS_NOT_FOUND = "The chat id was not found.";
    public static String NOT_FOUND_ADDRESS_BY_ORDER_ID = "Not found order id : ";

    /**
     * Constructor.
     */
    public ErrorMessage() {
    }
}
