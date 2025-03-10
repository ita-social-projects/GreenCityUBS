package greencity.mapping.location;

import greencity.dto.address.UpdateAddressDto;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.entity.user.ubs.OrderAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressToUpdateAddressDto extends AbstractConverter<OrderAddress, UpdateAddressDto> {
    @Override
    protected UpdateAddressDto convert(OrderAddress source) {
        return UpdateAddressDto.builder()
            .orderAddressExportDetails(
                OrderAddressExportDetailsDtoUpdate.builder()
                    .id(source.getId())
                    .districtUk(source.getAddress().getDistrictUk())
                    .districtEn(source.getAddress().getDistrictEn())
                    .streetUk(source.getAddress().getStreetUk())
                    .streetEn(source.getAddress().getStreetEn())
                    .houseCorpus(source.getAddress().getHouseCorpus())
                    .entranceNumber(source.getAddress().getEntranceNumber())
                    .houseNumber(source.getAddress().getHouseNumber())
                    .cityUk(source.getAddress().getCityUk())
                    .cityEn(source.getAddress().getCityEn())
                    .regionUk(source.getAddress().getRegionUk())
                    .regionEn(source.getAddress().getRegionEn())
                    .addressComment(source.getAddress().getAddressComment())
                    .build())
            .build();
    }
}
