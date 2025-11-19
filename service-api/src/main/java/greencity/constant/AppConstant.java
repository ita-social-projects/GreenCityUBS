package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstant {
    public static final String UKRAINE_TIMEZONE = "Europe/Kyiv";
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String ENROLLMENT_TO_THE_BONUS_ACCOUNT_UK = "Зарахування на бонусний рахунок";
    public static final String ENROLLMENT_TO_THE_BONUS_ACCOUNT_EN = "Enrollment to the bonus account";
    public static final String PAYMENT_REFUND_UK = "Повернення коштів";
    public static final String PAYMENT_REFUND_EN = "Money refund";
    public static final String UBS_LINK_USERPROFILE = "/ubs/userProfile";
    public static final String DEFAULT_IMAGE =
        "https://csb10032000a548f571.blob.core.windows.net/allfiles/90370622-3311-4ff1-9462-20cc98a64d1ddefault_image.jpg";
    public static final String UBS_LINK = "/ubs";
    public static final String UBS_MANAG_LINK = "/ubs/management";
    public static final String UBS_CLIENT_LINK = "/ubs/client";
    public static final String ADMIN_LINK = "/admin";
    public static final String NOTIFICATIONS_LINK = "/notifications";
    public static final String ADMIN_EMPL_LINK = "/admin/ubs-employee";
    public static final String SUPER_ADMIN_LINK = "/ubs/superAdmin";
    public static final String USER_AGREEMENT_LINK = "/user-agreement";
    public static final String UBS_EXPORT = UBS_LINK + "/order/pdf/export";
    public static final String LOGS_LINKS = "/logs/**";
    public static final String EXPORT_SETTINGS_LINKS = "/export/settings/**";
    public static final String TELEGRAM_LINK = "/ubs/telegram";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String UBS_EMPLOYEE = "UBS_EMPLOYEE";
    public static final String UBS_EMPLOYEE_WITH_PREFIX = "ROLE_UBS_EMPLOYEE";
    public static final String USER_WITH_PREFIX = "ROLE_USER";
    public static final String COMMIT_INFO = "/commit-info";
    public static final Integer TWO_DECIMALS_AFTER_POINT_IN_CURRENCY = 2;
    public static final Integer NO_DECIMALS_AFTER_POINT_IN_CURRENCY = 0;
    public static final String NOTIFICATOR_START_IS_FAILED_LOG_MESSAGE =
        "Failed to start scheduled notificator with type {} because cron is incorrect or template is inactive.";
    public static final String NOTIFICATOR_SUCCESSFULLY_START_LOG_MESSAGE =
        "Scheduled notificator for {} notification template by {} cron";
    public static final String NOTIFICATOR_RESTART_LOG_MESSAGE = "Restarting scheduled notificator {}";
    public static final String UNKNOWN_EN = "Unknown";
    public static final String UNKNOWN_UK = "Невідомо";
    public static final String LOCALE_UK_NAME = "uk";
    public static final String LOCALE_EN_NAME = "en";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String MESSAGE = "message";
    public static final String KYIV = "Kyiv";
    public static final String USER_SERVICE_UNAVAILABLE_LOG = "User service is unavailable: {}";
    public static final String FAILED_STATUS = "failure";
    public static final String APPROVED_STATUS = "Approved";
    public static final String TELEGRAM_PART_1_OF_LINK = "https://telegram.me/";
    public static final String TELEGRAM_PART_3_OF_LINK = "?start=";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_UK = "uk";
    public static final Double KYIV_LATITUDE = 50.4546600;
    public static final Double KYIV_LONGITUDE = 30.5238000;
    public static final Double LOCATION_40_KM_ZONE_VALUE = 40.00;
    public static final String UKRAINE_EN = "Ukraine";
    public static final String LANG_EN = "en";
    public static final String ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE = "Location and Address selected "
        + "does not match, reselect correct data.";
    public static final byte CURRENCY_CONVERSION_RATE = 100;
    public static final byte MAX_CERTIFICATES_PER_ORDER = 5;
    public static final Integer VALIDITY_DURATION_TEN_DAYS = 300;
    public static final String PAY_BUTTON = "payButton";
    public static final int ORDER_ID_INDEX = 0;
    public static final int COUNTER_ORDER_PAYMENT_ID_INDEX = 1;
    public static final int PAYMENT_ID_INDEX = 2;
}