package greencity.mapping;

import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.TariffsForLocationDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Region;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TariffsForLocationDtoMapper extends AbstractConverter<TariffsInfo, TariffsForLocationDto> {
    @Override
    public TariffsForLocationDto convert(TariffsInfo source) {
        Region region = source.getTariffLocations() != null
            ? source.getTariffLocations().iterator().next().getLocation().getRegion()
            : null;
        RegionDto regionDto = region != null ? RegionDto.builder().regionId(region.getId()).nameEn(region.getEnName())
            .nameUk(region.getUkrName()).build() : null;
        return TariffsForLocationDto.builder()
            .regionDto(regionDto)
            .locationsDtosList(source.getTariffLocations().stream()
                .map(location -> LocationsDtos.builder()
                    .locationId(location.getId())
                    .nameEn(location.getLocation().getNameEn())
                    .nameUk(location.getLocation().getNameUk())
                    .build())
                .collect(Collectors.toList()))
            .tariffInfoId(source.getId())
            .courierLimit(source.getCourierLimit().toString())
            .maxPriceOfOrder(source.getMaxPriceOfOrder())
            .minPriceOfOrder(source.getMinPriceOfOrder())
            .maxAmountOfBigBags(source.getMaxAmountOfBigBags())
            .minAmountOfBigBags(source.getMinAmountOfBigBags())
            .courierTranslationDtos(source.getCourier().getCourierTranslationList().stream()
                .map(x -> CourierTranslationDto.builder()
                    .languageCode(x.getLanguage().getCode())
                    .name(x.getName())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
