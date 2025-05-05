package greencity.constant.pdf;

import greencity.constant.AppConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Locale;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum PdfUnitsOfMeasurement {
    VOLUME("л", "l"),
    UNITS("шт.", "pc."),
    CURRENCY("грн", "UAH");

    private final String nameUk;
    private final String nameEn;

    public static String getByLocale(PdfUnitsOfMeasurement headerName, Locale locale) {
        if (Objects.equals(AppConstant.LOCALE_EN_NAME, locale.getLanguage())) {
            return headerName.nameEn;
        }
        return headerName.nameUk;
    }
}