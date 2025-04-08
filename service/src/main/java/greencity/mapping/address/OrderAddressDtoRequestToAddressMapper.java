package greencity.mapping.address;

import greencity.dto.order.OrderAddressDtoRequest;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.BaseAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressDtoRequestToAddressMapper extends AbstractConverter<OrderAddressDtoRequest, Address> {
    @Override
    protected Address convert(OrderAddressDtoRequest orderAddressDtoRequest) {
        return Address.builder()
            .id(orderAddressDtoRequest.getId())
            .coordinates(Coordinates.builder()
                .latitude(orderAddressDtoRequest.getCoordinates().getLatitude())
                .longitude(orderAddressDtoRequest.getCoordinates().getLongitude())
                .build())
            .baseAddress(BaseAddress.builder()
                .regionEn(orderAddressDtoRequest.getRegionEn())
                .regionUk(orderAddressDtoRequest.getRegionUk())
                .cityEn(orderAddressDtoRequest.getCityEn())
                .cityUk(orderAddressDtoRequest.getCityUk())
                .districtEn(orderAddressDtoRequest.getDistrictEn())
                .districtUk(orderAddressDtoRequest.getDistrictUk())
                .streetEn(orderAddressDtoRequest.getStreetEn())
                .streetUk(orderAddressDtoRequest.getStreetUk())
                .houseNumber(orderAddressDtoRequest.getHouseNumber())
                .houseCorpus(orderAddressDtoRequest.getHouseCorpus())
                .entranceNumber(orderAddressDtoRequest.getEntranceNumber())
                .build())
            .build();
    }
}
