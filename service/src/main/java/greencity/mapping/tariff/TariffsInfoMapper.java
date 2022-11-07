package greencity.mapping.tariff;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.tariff.TariffsInfoDto;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TariffsInfoMapper extends AbstractConverter<TariffsInfo, TariffsInfoDto> {
    @Override
    protected TariffsInfoDto convert(TariffsInfo tariffsInfo) {
        return TariffsInfoDto.builder()
            .id(tariffsInfo.getId())
            .minAmountOfBags(tariffsInfo.getMinAmountOfBigBags())
            .maxAmountOfBags(tariffsInfo.getMaxAmountOfBigBags())
            .minPriceOfOrder(tariffsInfo.getMinPriceOfOrder())
            .maxPriceOfOrder(tariffsInfo.getMaxPriceOfOrder())
            .courier(CourierDto.builder()
                .courierId(tariffsInfo.getCourier().getId())
                .courierTranslationDtos(tariffsInfo.getCourier().getCourierTranslationList().stream()
                    .map(translation -> CourierTranslationDto.builder()
                        .name(translation.getName())
                        .nameEng(translation.getNameEng())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .receivingStations(tariffsInfo.getReceivingStationList().stream()
                .map(s -> ReceivingStationDto.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .build())
                .collect(Collectors.toList()))
            .tariffLocations(tariffsInfo.getTariffLocations())
            .build();
    }
}