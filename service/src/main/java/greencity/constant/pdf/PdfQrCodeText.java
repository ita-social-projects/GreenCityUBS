package greencity.constant.pdf;

import greencity.constant.AppConstant;
import java.util.Locale;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PdfQrCodeText {
    QR_CODE_HINT("Проскануйте або натисніть на QR-код, щоб оплатити замовлення.",
                 "Scan or click the QR code to pay"),
    ALREADY_PAID("Це замовлення вже оплачене. Оплата не потрібна.",
                 "This order has already been paid. No payment is required."),
    LINK_NOT_GENERATED("Не вдалося згенерувати платіжне посилання для цього замовлення.",
                       "Failed to generate a payment link for this order.");

    private final String textUk;
    private final String textEn;

    public static String getByLocale(PdfQrCodeText label, Locale locale) {
        return Objects.equals(locale.getLanguage(), AppConstant.LOCALE_EN_NAME)
            ? label.textEn
            : label.textUk;
    }
}
