package greencity.mapping.location;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.courier.CourierDto;
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
            .locationsDtosList(source.getTariffLocations()
                .stream().map(tariffLocation -> LocationsDtos.builder()
                    .locationId(tariffLocation.getLocation().getId())
                    .nameEn(tariffLocation.getLocation().getNameEn())
                    .nameUk(tariffLocation.getLocation().getNameUk())
                    .build())
                .collect(Collectors.toList()))
            .tariffInfoId(source.getId())
            .courierDto(CourierDto.builder()
                .courierId(source.getCourier().getId())
                .nameEn(source.getCourier().getNameEn())
                .nameUk(source.getCourier().getNameUk())
                .courierStatus(source.getCourier().getCourierStatus().name())
                .build())
            .courierLimit(source.getCourierLimit())
            .max(source.getMax())
            .min(source.getMin())
            .limitDescription(source.getLimitDescription())
            .build();
    }
}
