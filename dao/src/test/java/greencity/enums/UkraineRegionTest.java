package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UkraineRegionTest {
    @Test
    void testDisplayName() {
        assertThat(UkraineRegion.VINNYTSIA_OBLAST.getDisplayName()).isEqualTo("Vinnyts'ka Oblast");
        assertThat(UkraineRegion.VOLYN_OBLAST.getDisplayName()).isEqualTo("Volyns'ka Oblast");
        assertThat(UkraineRegion.DONETSK_OBLAST.getDisplayName()).isEqualTo("Donets'ka Oblast");
        assertThat(UkraineRegion.IVANO_FRANKIVSK_OBLAST.getDisplayName()).isEqualTo("Ivano-Frankivs'ka Oblast");
        assertThat(UkraineRegion.KHERSON_OBLAST.getDisplayName()).isEqualTo("Khersons'ka Oblast");
        assertThat(UkraineRegion.KHMELNYTSKY_OBLAST.getDisplayName()).isEqualTo("Khmelnyts'ka Oblast");
        assertThat(UkraineRegion.KYIV_OBLAST.getDisplayName()).isEqualTo("Kyivs'ka oblast");
        assertThat(UkraineRegion.KYIV_CITY.getDisplayName()).isEqualTo("Kyiv");
        assertThat(UkraineRegion.LVIV_OBLAST.getDisplayName()).isEqualTo("Lvivs'ka Oblast");
        assertThat(UkraineRegion.MYKOLAIV_OBLAST.getDisplayName()).isEqualTo("Mykolaivs'ka Oblast");
        assertThat(UkraineRegion.ODESSA_OBLAST.getDisplayName()).isEqualTo("Odess'ka Oblast");
        assertThat(UkraineRegion.POLTAVA_OBLAST.getDisplayName()).isEqualTo("Poltavs'ka Oblast");
        assertThat(UkraineRegion.RIVNE_OBLAST.getDisplayName()).isEqualTo("Rivnens'ka Oblast");
        assertThat(UkraineRegion.SUMY_OBLAST.getDisplayName()).isEqualTo("Sums'ka Oblast");
        assertThat(UkraineRegion.TERNOPIL_OBLAST.getDisplayName()).isEqualTo("Ternopils'ka Oblast");
        assertThat(UkraineRegion.ZAPORIZHIA_OBLAST.getDisplayName()).isEqualTo("Zaporizs'ka Oblast");
        assertThat(UkraineRegion.ZHYTOMYR_OBLAST.getDisplayName()).isEqualTo("Zhytomyrs'ka Oblast");
        assertThat(UkraineRegion.KIROVOGRAD_OBLAST.getDisplayName()).isEqualTo("Kirovograds'ka Oblast");
        assertThat(UkraineRegion.CHERKASY_OBLAST.getDisplayName()).isEqualTo("Cherkas'ka Oblast");
        assertThat(UkraineRegion.CHERNIVTSI_OBLAST.getDisplayName()).isEqualTo("Chernivets'ka Oblast");
        assertThat(UkraineRegion.CHERNIHIV_OBLAST.getDisplayName()).isEqualTo("Chernihivs'ka Oblast");
        assertThat(UkraineRegion.DNIPRO_OBLAST.getDisplayName()).isEqualTo("Dnipropetrovs'ka Oblast");
        assertThat(UkraineRegion.KHARKIV_OBLAST.getDisplayName()).isEqualTo("Kharkivs'ka oblast");
        assertThat(UkraineRegion.CRIMEA.getDisplayName()).isEqualTo("Crimea");
    }

    @Test
    void testAllEnumValues() {
        UkraineRegion[] regions = UkraineRegion.values();

        assertThat(regions).hasSize(24);

        assertThat(regions).contains(
            UkraineRegion.VINNYTSIA_OBLAST,
            UkraineRegion.VOLYN_OBLAST,
            UkraineRegion.DONETSK_OBLAST,
            UkraineRegion.IVANO_FRANKIVSK_OBLAST,
            UkraineRegion.KHERSON_OBLAST,
            UkraineRegion.KHMELNYTSKY_OBLAST,
            UkraineRegion.KYIV_OBLAST,
            UkraineRegion.KYIV_CITY,
            UkraineRegion.LVIV_OBLAST,
            UkraineRegion.MYKOLAIV_OBLAST,
            UkraineRegion.ODESSA_OBLAST,
            UkraineRegion.POLTAVA_OBLAST,
            UkraineRegion.RIVNE_OBLAST,
            UkraineRegion.SUMY_OBLAST,
            UkraineRegion.TERNOPIL_OBLAST,
            UkraineRegion.ZAPORIZHIA_OBLAST,
            UkraineRegion.ZHYTOMYR_OBLAST,
            UkraineRegion.KIROVOGRAD_OBLAST,
            UkraineRegion.CHERKASY_OBLAST,
            UkraineRegion.CHERNIVTSI_OBLAST,
            UkraineRegion.CHERNIHIV_OBLAST,
            UkraineRegion.DNIPRO_OBLAST,
            UkraineRegion.KHARKIV_OBLAST,
            UkraineRegion.CRIMEA);
    }
}
