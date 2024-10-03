package greencity.constant;

public class ValidationConstant {
    public static final String SERTIFICATE_CODE_REGEXP = "\\d{4}-\\d{4}";
    public static final String SERTIFICATE_CODE_REGEXP_MESSAGE = "This sertifacate code is not valid";
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
    public static final String CH_UA = "[ЁёІіЇїҐґЄєА-Яа-я\\s-ʼ'`ʹ,.]";
    public static final String CH_NUM = "[-A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9.,ʼ'`ʹ—/\"\\s]";
    public static final String COURIER_NAME_EN_REGEXP = "^[A-Z][A-Za-zА0-9'\\s]{1,29}$";
    public static final String COURIER_NAME_UK_REGEXP = "^[ЁІЇҐЄА-Я][ЁёІіЇїҐґЄєА-Яа-яA[0-9]'\\s]{1,29}$";

    public static final String NAME_REGEXP =
        "^(?!.*[ъыёэЪЫЁЭ])[ґҐіІєЄїЇА-Яа-яa-zA-Z](?!.*\\\\.\\$)(?!.*?\\\\.\\\\.)"
            + "(?!.*?--)(?!.*?'')[-'ʼ’ ґҐіІєЄїЇА-Яа-я+\\\\w.]{0,29}$";
    public static final String STREET_REGEXP = "^(?![0-9]+$)[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ0-9-]*$";
    public static final String STREET_VALIDATION_MESSAGE =
        "Use only English, or Ukrainian letters. Both English or Ukrainian letters valid, "
            + "for cases, when user inputs street address by yourself instead of using Google Api, "
            + "in that cases sets the same value for both localizations.";

    public static final String INVALID_EMAIL = "Invalid email format";

    /**
     * Constructor.
     */
    private ValidationConstant() {
        // Do nothing because needed.
    }
}
