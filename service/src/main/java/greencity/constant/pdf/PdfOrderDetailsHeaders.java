package greencity.constant.pdf;

import greencity.constant.AppConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum PdfOrderDetailsHeaders {
    ORDER_NUMBER("№", "#"),
    ORDER_DATE("Дата замовлення", "Order date"),
    PAYMENT_DATE("Дата оплати", "Payment date"),
    ORDER_STATUS("Статус замовлення", "Order status"),
    PAYMENT_STATUS("Статус оплати", "Payment status"),
    PAYMENT_AMOUNT("Сума замовлення", "Payment amount"),
    AMOUNT_DUE("Сума до оплати", "Amount-due");

    private final String nameUa;
    private final String nameEn;

    public static String getByLocale(PdfOrderDetailsHeaders headerName, Locale locale) {
        if (Objects.equals(AppConstant.LOCALE_ENG_NAME, locale.getLanguage())) {
            return headerName.nameEn;
        }
        return headerName.nameUa;
    }

    public static List<String> getAllByLocale(Locale locale) {
        return Arrays.stream(PdfOrderDetailsHeaders.values())
            .map(value -> getByLocale(value, locale))
            .collect(Collectors.toList());
    }
}