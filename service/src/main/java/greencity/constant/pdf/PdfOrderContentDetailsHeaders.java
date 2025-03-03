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
public enum PdfOrderContentDetailsHeaders {
    SERVICE("Послуги", "Services"),
    VOLUME("Об'єм", "Volume"),
    COST("Вартість", "Cost"),
    QUANTITY("Кількість пакетів", "Quantity of bags"),
    SUM("Сума", "Sum");

    private final String nameUa;
    private final String nameEn;

    public static String getByLocale(PdfOrderContentDetailsHeaders headerName, Locale locale) {
        if (Objects.equals(AppConstant.LOCALE_ENG_NAME, locale.getLanguage())) {
            return headerName.nameEn;
        }
        return headerName.nameUa;
    }
    public static List<String> getAllByLocale(Locale locale) {
        return Arrays.stream(PdfOrderContentDetailsHeaders.values())
                .map(value -> getByLocale(value, locale))
                .collect(Collectors.toList());
    }
}
