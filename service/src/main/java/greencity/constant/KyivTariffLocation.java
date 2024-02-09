package greencity.constant;

import lombok.Getter;

@Getter
public enum KyivTariffLocation {
    KYIV("Kyiv"),
    HATNE("Hatne"),
    HORENKA("Horenka"),
    ZAZYMIE("Zazymie"),
    IRPIN("Irpin"),
    KNIAZHYCHI("Kniazhychi"),
    KOTSIUBYNSKE("Kotsiubyns'ke"),
    NOVOSILKY("Novosilky"),
    PETROPAVLIVSKA_BORSHCHAHIVKA("Petropavlivska Borshchahivka"),
    POHREBY("Pohreby"),
    PROLISKY("Prolisky"),
    SOFIIVSKA_BORSHCHAHIVKA("Sofiivska Borschahivka"),
    CHAIKY("Chaiky"),
    SHCHASLUVE("Shchaslyve");

    private final String locationName;

    KyivTariffLocation(String locationName) {
        this.locationName = locationName;
    }
}
