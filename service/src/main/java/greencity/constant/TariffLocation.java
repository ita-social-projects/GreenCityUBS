package greencity.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TariffLocation {
    KYIV_TARIFF(1L),
    KYIV_REGION_20_KM_TARIFF(2L);

    private final Long locationId;
}
