package greencity.constant;

public final class AppConstant {
    private AppConstant() {
    }

    public static final String ENROLLMENT_TO_THE_BONUS_ACCOUNT = "Зарахування на бонусний рахунок";
    public static final String PAYMENT_REFUND = "Повернення коштів";
    public static final String ubsLink = "/ubs/userProfile";
    public static final String DEFAULT_IMAGE =
        "https://csb10032000a548f571.blob.core.windows.net/allfiles/90370622-3311-4ff1-9462-20cc98a64d1ddefault_image.jpg";
    public static final String UBS_LINK = "/ubs";
    public static final String UBS_MANAG_LINK = "/ubs/management";
    public static final String ADMIN_LINK = "/admin";
    public static final String ADMIN_EMPL_LINK = "/admin/ubs-employee";
    public static final String SUPER_ADMIN_LINK = "/ubs/superAdmin";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String UBS_EMPLOYEE = "UBS_EMPLOYEE";
    public static final String USER_WITH_PREFIX = "ROLE_USER";
    public static final Integer TWO_DECIMALS_AFTER_POINT_IN_CURRENCY = 2;
    public static final Integer NO_DECIMALS_AFTER_POINT_IN_CURRENCY = 0;

    public static final String NOTIFICATOR_START_IS_FAILED_LOG_MESSAGE =
        "Failed to start scheduled notificator with type {} because cron is incorrect or template is inactive.";
    public static final String NOTIFICATOR_SUCCESSFULLY_START_LOG_MESSAGE =
        "Scheduled notificator for {} notification template by {} cron";
    public static final String NOTIFICATOR_RESTART_LOG_MESSAGE = "Restarting scheduled notificator {}";

    public static final String UNKNOWN_ENG = "Unknown";
    public static final String UNKNOWN_UA = "Невідомо";
}