package greencity.mapping.order;

import greencity.dto.order.OrderAddressDtoResponse;
import greencity.entity.user.ubs.OrderAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressDtoUpdateMapper extends AbstractConverter<OrderAddress, OrderAddressDtoResponse> {
    @Override
    protected OrderAddressDtoResponse convert(OrderAddress dtoUpdate) {
        return OrderAddressDtoResponse.builder()
            .district(dtoUpdate.getDistrict())
            .districtEng(dtoUpdate.getDistrictEn())
            .street(dtoUpdate.getStreet())
            .streetEng(dtoUpdate.getStreetEn())
            .houseCorpus(dtoUpdate.getHouseCorpus())
            .entranceNumber(dtoUpdate.getEntranceNumber())
            .houseNumber(dtoUpdate.getHouseNumber())
            .build();
    }
}