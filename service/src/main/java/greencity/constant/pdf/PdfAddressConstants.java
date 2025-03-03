package greencity.constant.pdf;

import greencity.constant.AppConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Locale;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum PdfAddressConstants {
    HOUSE_NUMBER("буд. ", "b. "),
    HOUSE_CORPUS_NUMBER("корпус ", "b. "),
    ENTRANCE_NUMBER("під'їзд ", "e. "),
    DISTRICT("район ", "d. ");

    private final String nameUa;
    private final String nameEn;

    public static String getByLocale(PdfAddressConstants headerName, Locale locale) {
        if (Objects.equals(AppConstant.LOCALE_ENG_NAME, locale.getLanguage())) {
            return headerName.nameEn;
        }
        return headerName.nameUa;
    }
}
