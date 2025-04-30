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
                    .district(source.getDistrict())
                    .districtEn(source.getDistrictEn())
                    .street(source.getStreet())
                    .streetEn(source.getStreetEn())
                    .houseCorpus(source.getHouseCorpus())
                    .entranceNumber(source.getEntranceNumber())
                    .houseNumber(source.getHouseNumber())
                    .city(source.getCity())
                    .cityEn(source.getCityEn())
                    .region(source.getRegion())
                    .regionEn(source.getRegionEn())
                    .addressComment(source.getAddressComment())
                    .build())
            .build();
    }
}
