package greencity.enums;

import lombok.Getter;

@Getter
public enum UkraineRegion {
    VINNYTSIA_OBLAST("Vinnyts'ka Oblast"),
    VOLYN_OBLAST("Volyns'ka Oblast"),
    DONETSK_OBLAST("Donets'ka Oblast"),
    IVANO_FRANKIVSK_OBLAST("Ivano-Frankivs'ka Oblast"),
    KHERSON_OBLAST("Khersons'ka Oblast"),
    KHMELNYTSKY_OBLAST("Khmelnyts'ka Oblast"),
    KYIV_OBLAST("Kyivs'ka oblast"),
    KYIV_CITY("Kyiv"),
    LVIV_OBLAST("Lvivs'ka Oblast"),
    MYKOLAIV_OBLAST("Mykolaivs'ka Oblast"),
    ODESSA_OBLAST("Odess'ka Oblast"),
    POLTAVA_OBLAST("Poltavs'ka Oblast"),
    RIVNE_OBLAST("Rivnens'ka Oblast"),
    SUMY_OBLAST("Sums'ka Oblast"),
    TERNOPIL_OBLAST("Ternopils'ka Oblast"),
    ZAPORIZHIA_OBLAST("Zaporizs'ka Oblast"),
    ZHYTOMYR_OBLAST("Zhytomyrs'ka Oblast"),
    KIROVOGRAD_OBLAST("Kirovograds'ka Oblast"),
    CHERKASY_OBLAST("Cherkas'ka Oblast"),
    CHERNIVTSI_OBLAST("Chernivets'ka Oblast"),
    CHERNIHIV_OBLAST("Chernihivs'ka Oblast"),
    DNIPRO_OBLAST("Dnipropetrovs'ka Oblast"),
    KHARKIV_OBLAST("Kharkivs'ka oblast"),
    CRIMEA("Crimea");

    private final String displayName;

    UkraineRegion(String displayName) {
        this.displayName = displayName;
    }
}
