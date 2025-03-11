package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.BaseAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CreateAddressRequestDtoToAddress extends AbstractConverter<CreateAddressRequestDto, Address> {
    @Override
    protected Address convert(CreateAddressRequestDto source) {
        return Address.builder()
            .baseAddress(BaseAddress.builder()
                .regionUk(source.getRegionUk())
                .regionEn(source.getRegionEn())
                .cityUk(source.getCityUk())
                .cityEn(source.getCityEn())
                .districtUk(source.getDistrictUk())
                .districtEn(source.getDistrictEn())
                .addressComment(source.getAddressComment())
                .houseNumber(source.getHouseNumber())
                .entranceNumber(source.getEntranceNumber())
                .houseCorpus(source.getHouseCorpus())
                .streetUk(source.getStreetUk())
                .streetEn(source.getStreetEn())
                .build())
            .coordinates(Coordinates.builder()
                .longitude(source.getCoordinates().getLongitude())
                .latitude(source.getCoordinates().getLatitude())
                .build())

            .build();
    }
}
