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
                    .districtUk(source.getBaseAddress().getDistrictUk())
                    .districtEn(source.getBaseAddress().getDistrictEn())
                    .streetUk(source.getBaseAddress().getStreetUk())
                    .streetEn(source.getBaseAddress().getStreetEn())
                    .houseCorpus(source.getBaseAddress().getHouseCorpus())
                    .entranceNumber(source.getBaseAddress().getEntranceNumber())
                    .houseNumber(source.getBaseAddress().getHouseNumber())
                    .cityUk(source.getBaseAddress().getCityUk())
                    .cityEn(source.getBaseAddress().getCityEn())
                    .regionUk(source.getBaseAddress().getRegionUk())
                    .regionEn(source.getBaseAddress().getRegionEn())
                    .addressComment(source.getBaseAddress().getAddressComment())
                    .build())
            .build();
    }
}
