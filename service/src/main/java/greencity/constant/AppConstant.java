package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AppConstant {
    public static final String UKRAINE_TIMEZONE = "Europe/Kyiv";
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String ENROLLMENT_TO_THE_BONUS_ACCOUNT = "Зарахування на бонусний рахунок";
    public static final String ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG = "Enrollment to the bonus account";
    public static final String PAYMENT_REFUND = "Повернення коштів";
    public static final String PAYMENT_REFUND_ENG = "Money refund";
    public static final String UBS_USER_PROFILE_LINK = "/ubs/userProfile";
    public static final String DEFAULT_IMAGE =
        "https://csb10032000a548f571.blob.core.windows.net/allfiles/90370622-3311-4ff1-9462-20cc98a64d1ddefault_image.jpg";
    public static final String UBS_LINK = "/ubs";
    public static final String CLIENT_LINK = "/client/**";
    public static final String USER_PROFILE_LINK = "/userProfile/**";
    public static final String UBS_MANAGE_LINK = "/ubs/management";
    public static final String ADMIN_LINK = "/admin";
    public static final String ADMIN_EMPLOYEE_LINK = "/admin/ubs-employee";
    public static final String SUPER_ADMIN_LINK = "/ubs/superAdmin";
    public static final String USER_AGREEMENT_LINK = "/user-agreement";
    public static final String COMMIT_INFO = "/commit-info";
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