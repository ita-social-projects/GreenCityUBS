package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessage {
    public static final String CERTIFICATE_EXIST = "Certificate with this code is already exist";
    public static final String CERTIFICATE_NOT_FOUND_BY_CODE = "Certificate does not exist by this code: ";
    public static final String CERTIFICATE_EXPIRED = "Certificate expired by this code: ";
    public static final String CERTIFICATE_STATUS = "Certificate has status 'EXPIRED' or 'USED'";
    public static final String CERTIFICATE_IS_USED = "The certificate has been used before or is not activated."
        + " Certificate code: ";
    public static final String CERTIFICATE_IS_NOT_ACTIVATED = "The certificate is not activated yet:";
    public static final String BAG_NOT_FOUND = "Bag does not exist by id: ";
    public static final String USER_DONT_HAVE_ENOUGH_POINTS = "User doesn't have enough bonus points.";
    public static final String TOO_MANY_CERTIFICATES = "Too many certificates was entered.";
    public static final String INVALID_DISTANCE_AMOUNT = "The distance should be between 0 and 20 km.";
    public static final String NO_SUCH_COORDINATES = "There are no any order with coordinates: ";
    public static final String INVALID_LITRES_AMOUNT = "The amount of litres should be between 0 and 10.000 litres.";
    public static final String NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER = "Not found address with such id: ";
    public static final String NOT_FOUND_ADDRESS_BY_CITY_AND_COUNTRY =
        "Not found address in current city and country: ";
    public static final String NOT_FOUND_ADDRESS_BY_PLACE_ID = "Not found address with such place id: ";
    public static final String UNDELIVERED_ORDERS_NOT_FOUND = "There are no any undelivered orders found.";
    public static final String PAYMENT_VALIDATION_ERROR = "The received payment data is not valid.";
    public static final String THE_MESSAGE_WAS_NOT_SENT = "The message was not sent.";
    public static final String USER_WITH_CURRENT_UUID_DOES_NOT_EXIST = "User with current uuid does not exist.";
    public static final String USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS =
        "User with current uuid already exists in UBS.";
    public static final String USER_WITH_CURRENT_ID_DOES_NOT_EXIST = "User with current id does not exist.";
    public static final String ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST = "Order with current id does not exist: ";
    public static final String RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST = "Recipient with current id does not exist.";
    public static final String NOT_FOUND_ADDRESS_BY_ORDER_ID = "Address not found for order by id: ";
    public static final String ORDER_STATUS_NOT_FOUND = "Order status not found";
    public static final String ORDER_PAYMENT_STATUS_NOT_FOUND = "Order payment status not found";
    public static final String EMPLOYEE_NOT_FOUND = "Employee with current id doesn't exist: ";
    public static final String EMPLOYEE_WITH_UUID_NOT_FOUND = "Employee with current uuid doesn't exist: ";
    public static final String EMPLOYEE_WITH_ENABLED_CHAT_NOT_FOUND_BY_TARIFF_ID =
        "No employees with enabled chat found for tariff id: ";
    public static final String ACTIVE_EMPLOYEE_WITH_CURRENT_EMAIL_ALREADY_EXISTS =
        "Active employee with this email already exists: ";
    public static final String PHONE_NUMBER_PARSING_FAIL = "Phone number parsing fail: ";
    public static final String CURRENT_POSITION_ALREADY_EXISTS = "Position with this name already exists: ";
    public static final String POSITION_NOT_FOUND_BY_ID = "Position with current id doesn't exist: ";
    public static final String POSITION_NOT_FOUND = "Position doesn't exist";
    public static final String RECEIVING_STATION_ALREADY_EXISTS = "Receiving station already exists: ";
    public static final String RECEIVING_STATION_NOT_FOUND_BY_ID = "Receiving station with current id doesn't exist: ";
    public static final String RECEIVING_STATION_NOT_FOUND = "Receiving station doesn't exist.";
    public static final String EMPLOYEES_ASSIGNED_POSITION = "There are employees assigned to this position.";
    public static final String EMPLOYEE_WAS_NOT_SUCCESSFULLY_SAVED = "Employee was not successfully saved";
    public static final String CANNOT_DELETE_DEFAULT_IMAGE = "You can't delete default image.";
    public static final String PAYMENT_NOT_FOUND = "Payment not found for order id: ";
    public static final String ADDRESS_ALREADY_EXISTS = "Address already exists";
    public static final String REGION_NOT_FOUND = "Region not found: ";
    public static final String CITY_NOT_FOUND = "City not found: ";
    public static final String ACTUAL_ADDRESS_NOT_FOUND = "Actual address not found";
    public static final String LOCATION_DOESNT_FOUND_BY_ID = "Location does not exist by id: ";
    public static final String LOCATIONS_BELONG_TO_DIFFERENT_REGIONS =
        "The locations belong to different regions. Please choose locations from the same region";
    public static final String ORDER_ALREADY_HAS_VIOLATION = "Current order already has violation";
    public static final String ORDER_ALREADY_PAID = "Current order is already paid";
    public static final String VIOLATION_DOES_NOT_EXIST = "Violation does not exist for current order";
    public static final String ORDER_HAS_NOT_VIOLATION = "Order has not violation";
    public static final String INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION =
        "Cannot add a violation to order with this status: ";
    public static final String INCOMPATIBLE_ORDER_STATUS_FOR_MONEY_REFUND =
        "Money refund cannot be processed";
    public static final String EVENTS_NOT_FOUND_EXCEPTION = "Events didn't find in order id: ";
    public static final String NOT_ENOUGH_BAGS_EXCEPTION = "Not enough bags, minimal amount is: ";
    public static final String NOTIFICATION_DOES_NOT_EXIST = "Notification does not exist";
    public static final String NOTIFICATION_STATUS_DOES_NOT_EXIST = "Notification status does not exist ";
    public static final String NOTIFICATION_DOES_NOT_BELONG_TO_USER = "This notification does not belong to user";
    public static final String EMPLOYEE_DOESNT_EXIST = "Employee with this email does not exist: ";
    public static final String SERVICE_IS_NOT_FOUND_BY_ID = "Couldn't found service with id: ";
    public static final String SERVICE_ALREADY_EXISTS = "Service already exists for tariff with id: ";
    public static final String LOCATION_STATUS_IS_ALREADY_EXIST =
        "Current location already has status that's you wanna chose";
    public static final String LOCATION_IS_DEACTIVATED_FOR_TARIFF = "Location is deactivated for tariff: ";
    public static final String COURIER_IS_NOT_FOUND_BY_ID = "Couldn't found courier by id: ";
    public static final String CANNOT_DEACTIVATE_COURIER = "Courier is already deactivated with id: ";
    public static final String TOO_MANY_BAGS_EXCEPTION = "You chose too many bags, maximum amount is: ";
    public static final String PRICE_OF_ORDER_GREATER_THAN_LIMIT =
        "The price of you're order without discount is greater than allowable limit: ";
    public static final String PRICE_OF_ORDER_LOWER_THAN_LIMIT =
        "The price of you're order without discount is lower than allowable limit: ";
    public static final String SOME_CERTIFICATES_ARE_INVALID =
        "SOME CERTIFICATES ARE INVALID. A valid certificate is listed here";
    public static final String CERTIFICATE_NOT_FOUND = "CERTIFICATE_NOT_FOUND";
    public static final String NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID = "Notification template not found by id: ";
    public static final String NOTIFICATION_PLATFORM_NOT_FOUND = "Notification platform not found";
    public static final String LOCATION_ALREADY_EXIST = " that you try to add was already created early";
    public static final String INCORRECT_ECO_NUMBER = "Incorrect format of Eco number";
    public static final String COURIER_ALREADY_EXISTS = "Courier with this name already exists";
    public static final String ORDER_HAS_NO_OVERPAYMENT = "This order has no overpayment";
    public static final String INVALID_REQUESTED_REFUND_AMOUNT = "Invalid requested refund amount";
    public static final String CANNOT_REFUND_MONEY =
        "Money refund cannot be processed more than once for the same order";
    public static final String CANNOT_DELETE_ADDRESS = "Cannot delete another user's address";
    public static final String CANNOT_DELETE_ALREADY_DELETED_ADDRESS =
        "Cannot delete an address that has already been deleted.";
    public static final String CANNOT_MAKE_ACTUAL_DELETED_ADDRESS =
        "Cannot make actual address that has already been deleted.";
    public static final String CANNOT_ACCESS_PERSONAL_INFO = "Cannot access another user's personal info";
    public static final String CANNOT_ACCESS_ORDER_CANCELLATION_REASON =
        "Cannot access another user's order cancellation reason";
    public static final String CANNOT_CREATE_TARIFF = "Cannot create tariff. Courier is deactivated with id: ";
    public static final String USER_WITH_THIS_EMAIL_DOES_NOT_EXIST = "User with this email does not exist: ";
    public static final String LANGUAGE_ERROR = "Invalid language code";
    public static final String TARIFF_NOT_FOUND = "Couldn't found tariff with id: ";
    public static final String TARIFF_FOR_LOCATION_NOT_EXIST = "Could not find tariff for location with id: ";
    public static final String TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST =
        "Could not find tariff for courier with id: %d and location with id: %d ";
    public static final String TARIFF_OR_LOCATION_IS_DEACTIVATED = "Tariff or location is deactivated.";
    public static final String TARIFF_IS_ALREADY_EXISTS = "Tariff for such locations is already exists";
    public static final String COULD_NOT_RETRIEVE_PASSWORD_STATUS = "Could not retrieve password status";
    public static final String COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE = "Could not retrieve checkout response";
    public static final String COULD_NOT_RETRIEVE_USER_DATA = "Could not retrieve user data";
    public static final String COULD_NOT_RETRIEVE_EMPLOYEE_AUTHORITY = "Could not retrieve employee's authority";
    public static final String EMPLOYEE_AUTHORITY_WAS_NOT_EDITED = "Employee's authority was not edited";
    public static final String EMPLOYEE_EMAIL_WAS_NOT_EDITED = "Employee's email was not edited";
    public static final String EMPLOYEE_WAS_NOT_UPDATED = "Employee was not updated";
    public static final String TOO_MUCH_POINTS_FOR_ORDER = "Too much points for order, maximum amount: ";
    public static final String TARIFF_FOR_ORDER_NOT_EXIST = "Could not find tariff for order with id: ";
    public static final String CANNOT_ACCESS_ORDER_FOR_EMPLOYEE = "Cannot access order with id: ";
    public static final String NUMBER_OF_ADDRESSES_EXCEEDED = "Number of addresses reached maximum";
    public static final String TARIFF_LIMITS_ARE_INPUTTED_INCORRECTLY =
        "Limits are not inputted properly. You should input min and/or max limit values for at least one package.";
    public static final String MAX_VALUE_IS_INCORRECT = "Max value should be greater than min";
    public static final String MIN_MAX_VALUE_RESTRICTION = "Min and Max fields must have different values";
    public static final String BAG_FOR_TARIFF_NOT_EXIST = "Could not find bag with id %d for tariff with id %d";
    public static final String TARIFF_ALREADY_HAS_THIS_STATUS = "Tariff with id %d already has status: %s";
    public static final String TARIFF_ACTIVATION_RESTRICTION_DUE_TO_UNSPECIFIED_LIMITS =
        "Tariff has not been activated. Please set limits for tariff.";
    public static final String TARIFF_ACTIVATION_RESTRICTION_DUE_TO_UNSPECIFIED_BAGS =
        "Tariff has not been activated. Please add package for tariff.";
    public static final String TARIFF_ACTIVATION_RESTRICTION_DUE_TO_DEACTIVATED_COURIER =
        "Tariff has not been activated. Courier deactivated with id: ";
    public static final String TARIFF_EDIT_RESTRICTION_DUE_TO_DEACTIVATED_COURIER =
        "Tariff has not been edited. Courier deactivated with id: ";
    public static final String UNRESOLVABLE_TARIFF_STATUS = "Unresolvable tariff status. Please choose Active "
        + "or Deactivated.";
    public static final String UNRESOLVABLE_ACTIVATION_STATUS = "Unresolvable activation status. Please choose Active "
        + "or Deactivated.";
    public static final String DATE_OF_EXPORT_NOT_SPECIFIED_FOR_ORDER =
        "Date of export not specified for the order with ID: ";
    public static final String EMPTY_ORDERS_ID_COLLECTION = "Request should contain at least one order ID";
    public static final String ORDER_IS_BLOCKED = "The order has been blocked by employee with ID: ";
    public static final String REGIONS_NOT_FOUND_BY_LOCATION_STATUS =
        "Regions containing locations with a status: %s, not found";
    public static final String ORDER_CAN_NOT_BE_UPDATED = "An order with the status: %s, can not be updated";
    public static final String EMPLOYEE_NOT_FOUND_BY_EMAIL = "Employee with current id doesn't exist: ";
    public static final String ORDER_DOES_NOT_BELONG_TO_USER = "This order does not belong to user";
    public static final String TEMPLATE_DOES_NOT_CONTAIN_ALL_PLATFORMS =
        "This template does not contain messages for all platforms: ";
    public static final String PREDEFINED_NOTIFICATION_CANNOT_BE_DELETED =
        "Predefined notification template cannot be deleted.";
    public static final String TARIFF_NOT_FOUND_BY_LOCATION_ID = "Tariff with location id: %d not found";
    public static final String USER_AGREEMENT_NOT_FOUND_BY_ID = "User Agreement with ID %d not found";
    public static final String USER_NOT_FOUND_BY_UUID = "User not found by uuid: ";
    public static final String REFUND_CONFLICT_MONEY_AND_BONUSES =
        "A refund cannot be processed with both money and bonuses at the same time";
    public static final String TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST =
        "Could not find tariff information for bags with IDs %s at location with id: %d";
    public static final String COULD_NOT_RETRIEVE_USER_LANGUAGE = "Could not retrieve user language";
    public static final String CANNOT_CHANGE_ORDER_TABLE_VIEW =
        "You can't change order table view when table is frozen";
    public static final String TABLE_COLUMN_WIDTH_BY_EMPLOYEE_ID_NOT_FOUND =
        "Table column width by employee id not found";
    public static final String ORDER_STATUS_INVALID = "Order with ID %d cannot be updated due to its current status.";
    public static final String WARNING_GIT_DIRECTORY_NOT_FOUND =
        "WARNING: .git directory not found. Git commit info will be unavailable.";
    public static final String GIT_REPOSITORY_NOT_INITIALIZED =
        "Git repository not initialized. Commit info is unavailable.";
    public static final String FAILED_TO_FETCH_COMMIT_INFO = "Failed to fetch commit info due to I/O error: ";
    public static final String MAPPER_ERROR = "Data mapping error occurred. Please verify the input.";
    public static final String UNSUPPORTED_TYPE = "Unsupported target type: ";
    public static final String NOT_FOUND_ADDRESS_BY_ID = "Address with ID %d not found";
    public static final String CANNOT_EXPORT_DATA_TO_PDF = "An error occurred while exporting to PDF.";
    public static final String ORDER_NOT_FOUND_BY_PAYMENT_ID = "Order not found for payment id: %s";
    public static final String ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED =
        "Cannot update order: status must be FORMED and payment must be UNPAID";
    public static final String ORDER_NOT_FOUND_BY_ID = "Order not found with id: ";
    public static final String UUID_NOT_FOUND_BY_EMAIL = "UUID not found by email: ";
    public static final String TELEGRAM_BOT_ERROR_BAD_CREDENTIALS = "Bad credentials";
    public static final String TARIFF_VALIDATION_ERROR_MESSAGE = "Invalid tariff list.";
    public static final String POSITION_VALIDATION_ERROR_MESSAGE = "Invalid position IDs.";
    public static final String INVALID_POSITION_IDS = "Invalid position IDs: %s";
    public static final String TARIFF_LIST_IS_NULL = "Tariffs cannot be null";
    public static final String TARIFF_LIST_IS_EMPTY = "Tariffs cannot be empty";
    public static final String TARIFF_IS_NULL = "Tariff cannot be null";
    public static final String TARIFFID_IS_NULL = "Tariff id cannot be null";
    public static final String TARIFFID_IS_NOT_POSITIVE = "Tariff Id must be greater than 0";
    public static final String TARIFF_LIST_CONTAINS_DUPLICATES = "Tariffs cannot contain duplicates";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String TELEGRAM_SEND_EXCEPTION = "Exception occurred while sending to telegram: %s";
    public static final String TELEGRAM_RECEIVE_EXCEPTION = "Exception occurred while receiving from telegram: %s";
    public static final String TELEGRAM_NULL_METHOD_EXCEPTION = "Execution method cannot be null";
    public static final String TELEGRAM_INVALID_METHOD_EXCEPTION = "Invalid execution method type: %s";
    public static final String USER_DOESNT_HAVE_TELEGRAM_CHAT = "The user does not have a Telegram chat";
    public static final String TEMPLATE_IN_USE_CANNOT_BE_DELETED =
        "Template cannot be deleted while it is referenced by existing notifications";
    public static final String FORBIDDEN_USER_DELETION = "Blocked or deactivated user cannot be deleted";
    public static final String USER_CANNOT_DEACTIVATE_YOURSELF = "You cannot deactivate yourself if you are not a USER";
    public static final String ADMIN_CANNOT_DEACTIVATE_OTHER_ADMIN = "ADMIN cannot deactivate other ADMIN users";
    public static final String USER_DEACTIVATION_REASON_IS_EMPTY = "The User deactivation reasons list is empty";
}
