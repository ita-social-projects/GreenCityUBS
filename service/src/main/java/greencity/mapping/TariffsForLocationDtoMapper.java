package greencity.mapping;

import greencity.dto.CourierTranslationDto;
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
    public TariffsForLocationDto convert(TariffsInfo sourse) {
        Region region = sourse.getLocations().iterator().next().getRegion();
        RegionDto regionDto =
            RegionDto.builder().regionId(region.getId()).nameEn(region.getEnName()).nameUk(region.getUkrName()).build();
        return TariffsForLocationDto.builder()
            .regionDto(regionDto)
            .locationsDtosList(sourse.getLocations().stream()
                .map(location -> LocationsDtos.builder()
                    .locationId(location.getId())
                    .nameEn(location.getNameEn())
                    .nameUk(location.getNameUk())
                    .build())
                .collect(Collectors.toList()))
            .tariffInfoId(sourse.getId())
            .courierLimit(sourse.getCourierLimit().toString())
            .maxPriceOfOrder(sourse.getMaxPriceOfOrder())
            .minPriceOfOrder(sourse.getMinPriceOfOrder())
            .maxAmountOfBigBags(sourse.getMaxAmountOfBigBags())
            .minAmountOfBigBags(sourse.getMinAmountOfBigBags())
            .courierTranslationDtos(sourse.getCourier().getCourierTranslationList().stream()
                .map(x -> CourierTranslationDto.builder()
                    .languageCode(x.getLanguage().getCode())
                    .name(x.getName())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
