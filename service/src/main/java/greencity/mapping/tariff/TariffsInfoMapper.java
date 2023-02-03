package greencity.mapping.tariff;

import greencity.dto.courier.CourierDto;
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
            .min(tariffsInfo.getMin())
            .max(tariffsInfo.getMax())
            .courierLimit(tariffsInfo.getCourierLimit())
            .courier(CourierDto.builder()
                .courierId(tariffsInfo.getCourier().getId())
                .courierStatus(tariffsInfo.getCourier().getCourierStatus().name())
                .nameUk(tariffsInfo.getCourier().getNameUk())
                .nameEn(tariffsInfo.getCourier().getNameEn())
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
