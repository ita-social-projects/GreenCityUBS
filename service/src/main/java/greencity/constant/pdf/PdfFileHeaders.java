package greencity.constant.pdf;

import greencity.constant.AppConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum PdfFileHeaders {
    ORDER_DETAILS("Деталі замовлення: №", "Order details: #"),
    SENDER_INFO("Відправник", "Sender"),
    ORDER_COMMENT("Коментар до замовлення", "Comment to the order"),
    ADDRESS_INFO("Адреса вивезення відходів", "The address of export of the ordered services");

    private final String nameUk;
    private final String nameEn;

    public static String getByLocale(PdfFileHeaders headerName, Locale locale) {
        if (locale.getLanguage().equals(AppConstant.LOCALE_EN_NAME)) {
            return headerName.nameEn;
        }
        return headerName.nameUk;
    }
}