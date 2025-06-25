package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.order.OrderAddressDtoRequest;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressDtoRequestToCreateAddressRequestDto
    extends AbstractConverter<OrderAddressDtoRequest, CreateAddressRequestDto> {
    @Override
    protected CreateAddressRequestDto convert(OrderAddressDtoRequest source) {
        return CreateAddressRequestDto.builder()
            .districtEn(source.getDistrictEn())
            .districtUk(source.getDistrictUk())
            .regionEn(source.getRegionEn())
            .regionUk(source.getRegionUk())
            .houseNumber(source.getHouseNumber())
            .entranceNumber(source.getEntranceNumber())
            .houseCorpus(source.getHouseCorpus())
            .addressComment(source.getAddressComment())
            .placeId(source.getPlaceId())
            .coordinates(source.getCoordinates())
            .cityUk(source.getCityUk())
            .cityEn(source.getCityEn())
            .streetUk(source.getStreetUk())
            .streetEn(source.getStreetEn())
            .build();
    }
}
