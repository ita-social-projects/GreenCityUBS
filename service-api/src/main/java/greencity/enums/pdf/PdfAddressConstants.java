package greencity.enums.pdf;

import greencity.constant.AppConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Locale;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum PdfAddressConstants {
    HOUSE_NUMBER("буд.", "b."),
    HOUSE_CORPUS_NUMBER("корпус", "housing"),
    ENTRANCE_NUMBER("під'їзд", "entrance"),
    DISTRICT("район", "district");

    private final String nameUk;
    private final String nameEn;

    public static String getByLocale(PdfAddressConstants headerName, Locale locale) {
        if (Objects.equals(AppConstant.LOCALE_EN_NAME, locale.getLanguage())) {
            return headerName.nameEn;
        }
        return headerName.nameUk;
    }
}