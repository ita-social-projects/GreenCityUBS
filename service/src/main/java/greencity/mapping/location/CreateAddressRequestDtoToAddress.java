package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CreateAddressRequestDtoToAddress extends AbstractConverter<CreateAddressRequestDto, Address> {
    @Override
    protected Address convert(CreateAddressRequestDto source) {
        return Address.builder()
            .region(source.getRegion())
            .regionEn(source.getRegionEn())
            .city(source.getCity())
            .cityEn(source.getCityEn())
            .district(source.getDistrict())
            .districtEn(source.getDistrictEn())
            .addressComment(source.getAddressComment())
            .houseNumber(source.getHouseNumber())
            .entranceNumber(source.getEntranceNumber())
            .houseCorpus(source.getHouseCorpus())
            .coordinates(Coordinates.builder()
                .longitude(source.getCoordinates().getLongitude())
                .latitude(source.getCoordinates().getLatitude())
                .build())
            .street(source.getStreet())
            .streetEn(source.getStreetEn())
            .build();
    }
}
