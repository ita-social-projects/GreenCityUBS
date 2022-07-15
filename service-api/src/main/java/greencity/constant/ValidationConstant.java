package greencity.constant;

public class ValidationConstant {
    public static final String SERTIFICATE_CODE_REGEXP = "\\d{4}-\\d{4}";
    public static final String SERTIFICATE_CODE_REGEXP_MESSAGE = "This sertifacate code is not valid";
    public static final String SELECT_CORRECT_LANGUAGE = "Select correct language: 'en', 'ua' or 'ru'";
    public static final String CITY_UK_REGEXP =
        "^([А-ЯЇІЄҐ][а-яіїєґ]{0,39}'?[а-яіїєґ]{1,39}($|[ -](?=[А-ЯЇІЄҐ]))){1,10}$";
    public static final String CITY_EN_REGEXP = "^([A-Z][a-z]{0,39}'?[a-z]{1,39}($|[ -](?=[A-Z]))){1,10}$";

    /**
     * Constructor.
     */
    private ValidationConstant() {
        // Do nothing because needed.
    }
}
