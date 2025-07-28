package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationConstant {
    public static final String EMAIL_REGEXP =
        "^(?=.{3,72}$)"
            + "([a-zA-Z0-9!#$%&'*+/=?^_{|}~-]+"
            + "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_{|}~-]+)*)"
            + "@"
            + "(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+"
            + "[a-zA-Z]{2,63}|"
            + "\\[(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)"
            + "(?:\\.(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)){3}\\])$";
    public static final String CERTIFICATE_CODE_REGEXP = "\\d{4}-\\d{4}";
    public static final String CERTIFICATE_CODE_REGEXP_MESSAGE = "This certificate code is not valid";
    public static final String SELECT_CORRECT_LANGUAGE = "Select correct language: 'en' or 'ua'";
    public static final String COURIER_NAME_EN_MESSAGE = "use English letters, no longer than 30 symbols, "
        + "name cannot starts with a number or not a capital letter and could contain numbers and whitespaces";
    public static final String COURIER_NAME_UK_MESSAGE = "use Ukrainian letters, no longer than 30 symbols, "
        + "name cannot starts with a number or not a capital letter and could contain numbers and whitespaces";
    public static final String CITY_UK_REGEXP =
        "^([А-ЯЇІЄҐ][а-яіїєґ]{0,39}[ʼ'`ʹ]?[а-яіїєґ]{1,39}($|[ -](?=[А-ЯЇІЄҐ]))){1,10}$";
    public static final String CITY_EN_REGEXP =
        "^([A-Z][a-z]{0,39}[ʼ'`ʹ]?[a-z]{0,39}'?[a-z]{0,39}($|[ -](?=[A-Z]))){1,10}$";
    public static final String CH_EN = "[A-Za-z\\s-ʼ'`ʹ,.]";
    public static final String CH_UK = "[ЁёІіЇїҐґЄєА-Яа-я\\s-ʼ'`ʹ,.]";
    public static final String CH_NUM = "^([A-Za-zА-Яа-яЇїЄєІіҐґ0-9]([\\-/,]?))";
    public static final String COURIER_NAME_EN_REGEXP = "^[A-Z][A-Za-zА0-9'\\s]{1,29}$";
    public static final String COURIER_NAME_UK_REGEXP = "^[ЁІЇҐЄА-Я][ЁёІіЇїҐґЄєА-Яа-яA[0-9]'\\s]{1,29}$";

    public static final String NAME_REGEXP =
        "^[ґҐіІєЄїЇА-Яа-яa-zA-Z]"
            + "(?!.*\\.$)"
            + "(?!.*\\.\\.)"
            + "(?!.*--)"
            + "(?!.*'')"
            + "(?!.*(?:[-'ʼ’\\.]\\s+[-'ʼ’\\.]))"
            + "[-'ʼ’ ґҐіІєЄїЇА-Яа-я\\w\\.]{0,29}$";
    public static final String NAME_VALIDATION_MESSAGE =
        "Name must start with an English or Ukrainian letter, "
            + "be 1 to 30 characters long, "
            + "cannot end with a dot, "
            + "and cannot contain consecutive dots, dashes or apostrophes. "
            + "Allowed: English/Ukrainian letters, digits, underscore, space, dot, hyphen and apostrophe.";
    public static final String STREET_REGEXP = "^(?![0-9]+$)[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ0-9-]*$";
    public static final String STREET_VALIDATION_MESSAGE =
        "Use only English, or Ukrainian letters. Both English or Ukrainian letters valid, "
            + "for cases, when user inputs street address by yourself instead of using Google Api, "
            + "in that cases sets the same value for both localizations.";
    public static final String ADDRESS_VALIDATION_ERROR_MESSAGE = "Invalid data for address";
    public static final String USERNAME_REGEXP = """
        ^(?!.*\\.\\.)(?!.*\\.$)(?!.*\\-\\-)\
        (?=[ЄІЇҐЁєіїґёА-Яа-яA-Za-z])\
        [ЄІЇҐЁєіїґёА-Яа-яA-Za-z0-9\\s\\-'\\"’.ʼ]\
        {1,30}\
        (?<![ЭэЁёъЪЫы])$\
        """;
    public static final String NAMESURNAME_REGEXP = "^[A-Za-zА-Яа-я\\-'\\s]+$";
    public static final String USERNAME_MESSAGE = """
        Name must start with a letter, \
        cannot end with dot \
        or contain 2 consecutive dots, dashes and special symbols. \
        Use English or Ukrainian letters, \
        no longer than 30 symbols.\
        """;
    public static final String PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE =
        "Payment date should be set after the order creation.";
    public static final String PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE =
        "Payment date cannot be set after in the future.";
    public static final String PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE =
        "Provided payment date format is not a valid.";
    public static final String VALIDATION_RESPONSE_HEADER = "Following violation occurred during validation: ";
    public static final String VIOLATION_CHUNK = "{%s}";
}
