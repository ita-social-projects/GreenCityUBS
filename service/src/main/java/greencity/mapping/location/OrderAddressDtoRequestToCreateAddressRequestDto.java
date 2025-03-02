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
            .district(source.getDistrict())
            .regionEn(source.getRegionEn())
            .region(source.getRegion())
            .houseNumber(source.getHouseNumber())
            .entranceNumber(source.getEntranceNumber())
            .houseCorpus(source.getHouseCorpus())
            .addressComment(source.getAddressComment())
            .placeId(source.getPlaceId())
            .coordinates(source.getCoordinates())
            .city(source.getCity())
            .cityEn(source.getCityEn())
            .street(source.getStreet())
            .streetEn(source.getStreetEn())
            .build();
    }
}
