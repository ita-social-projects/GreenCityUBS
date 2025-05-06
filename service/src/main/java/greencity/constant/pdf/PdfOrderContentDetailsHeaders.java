
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
    VOLUME("Об'єм, "
        + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.VOLUME, Locale.of(AppConstant.LOCALE_UK_NAME)),
           "Volume, " + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.VOLUME,
               Locale.of(AppConstant.LOCALE_EN_NAME))),
    COST("Вартість, "
        + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.CURRENCY, Locale.of(AppConstant.LOCALE_UK_NAME)),
         "Cost, " + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.CURRENCY,
             Locale.of(AppConstant.LOCALE_EN_NAME))),
    QUANTITY("Кількість пакетів, "
        + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.UNITS, Locale.of(AppConstant.LOCALE_UK_NAME)),
             "Quantity of bags, " + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.UNITS,
                 Locale.of(AppConstant.LOCALE_EN_NAME))),
    SUM("Сума, "
        + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.CURRENCY, Locale.of(AppConstant.LOCALE_UK_NAME)),
        "Sum, " + PdfUnitsOfMeasurement.getByLocale(PdfUnitsOfMeasurement.CURRENCY,
            Locale.of(AppConstant.LOCALE_EN_NAME)));

    private final String nameUk;
    private final String nameEn;

    public static String getByLocale(PdfOrderContentDetailsHeaders headerName, Locale locale) {
        if (Objects.equals(AppConstant.LOCALE_EN_NAME, locale.getLanguage())) {
            return headerName.nameEn;
        }
        return headerName.nameUk;
    }

    public static List<String> getAllByLocale(Locale locale) {
        return Arrays.stream(PdfOrderContentDetailsHeaders.values())
            .map(value -> getByLocale(value, locale))
            .collect(Collectors.toList());
    }
}