package greencity.constant;

public class ErrorMessage {
    public static String CERTIFICATE_NOT_FOUND_BY_CODE = "Certificate does not exist by this code: ";
    public static String CERTIFICATE_EXPIRED = "Certificate expired by this code: ";
    public static String CERTIFICATE_IS_USED = "The certificate has been used before or is not activated."
        + " Certificate code: ";
    public static String BAG_NOT_FOUND = "Bag does not exist by id: ";
    public static String USER_DONT_HAVE_ENOUGH_POINTS = "User doesn't have enough bonus points.";
    public static String TOO_MANY_CERTIFICATES = "Too many certificates was entered.";
    public static String SUM_IS_COVERED_BY_CERTIFICATES = "Bonus points shouldn't be used if sum to pay is "
        + "covered by certificates.";
    public static String THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST = "The set of user data does not exist with id: ";
    public static String INAVALID_DISTANCE_AMOUNT = "The distance should be between 0 and 20 km.";
    public static String NO_SUCH_COORDINATES = "There are no any order with coordinates: ";
    public static String INAVALID_LITRES_AMOUNT = "The amount of litres should be between 0 and 10.000 litres.";
    public static String UNDELIVERED_ORDERS_NOT_FOUND = "There are no any undelivered orders found.";
    public static String MINIMAL_SUM_VIOLATION = "The minimal order sum should be 500 UAH.";

    /**
     * Constructor.
     */
    public ErrorMessage() {
    }
}
