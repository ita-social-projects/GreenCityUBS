package greencity.mapping.tariff;

import greencity.dto.location.LocationsForTariffDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.enums.LocationStatus;
import greencity.mapping.location.LocationToLocationsForTariffDtoMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetActiveTariffInfoDtoMapper extends AbstractConverter<TariffsInfo, GetActiveTariffInfoDto> {
    private final LocationToLocationsForTariffDtoMapper locationToLocationsForTariffDtoMapper;

    @Override
    protected GetActiveTariffInfoDto convert(TariffsInfo source) {
        List<LocationsForTariffDto> locationsForTariffDtos = Optional
            .ofNullable(source.getTariffLocations())
            .orElse(Set.of())
            .stream()
            .filter(tl -> tl != null && tl.getLocationStatus() == LocationStatus.ACTIVE)
            .map(TariffLocation::getLocation)
            .filter(java.util.Objects::nonNull)
            .map(locationToLocationsForTariffDtoMapper::convert)
            .filter(java.util.Objects::nonNull)
            .toList();

        GetActiveTariffInfoDto.GetActiveTariffInfoDtoBuilder builder = GetActiveTariffInfoDto.builder()
            .id(source.getId())
            .tariffNameEn(source.getTariffNameEn())
            .tariffNameUk(source.getTariffNameUk())
            .tariffLocations(locationsForTariffDtos);
        return builder.build();
    }
}
