package greencity.mapping;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Region;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GetTariffsInfoDtoMapper extends AbstractConverter<TariffsInfo, GetTariffsInfoDto> {
    @Override
    protected GetTariffsInfoDto convert(TariffsInfo source) {
        Region region = source.getLocations().iterator().next().getRegion();
        RegionDto regionDto =
            RegionDto.builder().regionId(region.getId()).nameEn(region.getEnName()).nameUk(region.getUkrName()).build();
        return GetTariffsInfoDto.builder()
            .cardId(source.getId())
            .courierLimit(source.getCourierLimit().toString())
            .maxAmountOfBags(source.getMaxAmountOfBigBags())
            .minAmountOfBags(source.getMinAmountOfBigBags())
            .maxPriceOfOrder(source.getMaxPriceOfOrder())
            .minPriceOfOrder(source.getMinPriceOfOrder())
            .regionDto(regionDto)
            .courierTranslationDtos(source.getCourier().getCourierTranslationList().stream()
                .map(courierTranslation -> CourierTranslationDto.builder()
                    .name(courierTranslation.getName())
                    .languageCode(courierTranslation.getLanguage().getCode())
                    .build())
                .collect(Collectors.toList()))
            .createdAt(source.getCreatedAt())
            // .creator(source.getCreator().getRecipientEmail())
            .tariffStatus(source.getLocationStatus().toString())
            .locationInfoDtos(source.getLocations().stream()
                .map(location -> LocationsDtos.builder()
                    .locationId(location.getId())
                    .nameEn(location.getNameEn())
                    .nameUk(location.getNameUk())
                    .build())
                .collect(Collectors.toList()))
            .receivingStationDtos(source.getReceivingStationList().stream()
                .map(receivingStation -> ReceivingStationDto.builder()
                    .id(receivingStation.getId())
                    .createDate(receivingStation.getCreateDate())
                    .name(receivingStation.getName())
                    // .createdBy(receivingStation.getCreatedBy().getRecipientEmail())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
