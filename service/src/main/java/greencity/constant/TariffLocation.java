package greencity.constant;

import lombok.Getter;

@Getter
public enum TariffLocation {
    KYIV_TARIFF(1L),
    KYIV_REGION_20_KM_TARIFF(2L);

    private final Long locationId;

    TariffLocation(Long locationId) {
        this.locationId = locationId;
    }
}
