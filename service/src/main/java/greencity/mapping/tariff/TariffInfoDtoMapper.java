package greencity.mapping.tariff;

import greencity.dto.TariffInfoDto;
import greencity.dto.courier.CourierDto;
import greencity.entity.order.Courier;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class TariffInfoDtoMapper extends AbstractConverter<TariffsInfo, TariffInfoDto> {
    @Override
    protected TariffInfoDto convert(TariffsInfo source) {
        if (source == null) {
            return null;
        }
        return TariffInfoDto.builder()
            .tariffInfoId(source.getId())
            .min(source.getMin())
            .max(source.getMax())
            .courierLimit(source.getCourierLimit())
            .courierDto(convertCourierToDto(source.getCourier()))
            .limitDescription(source.getLimitDescription())
            .build();
    }

    private CourierDto convertCourierToDto(Courier source) {
        return CourierDto.builder()
            .courierId(source.getId())
            .courierStatus(source.getCourierStatus().toString())
            .nameUk(source.getNameUk())
            .nameEn(source.getNameEn())
            .createDate(source.getCreateDate())
            .createdBy(source.getCreatedBy().getEmail())
            .build();
    }
}
