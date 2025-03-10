package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.BaseAddress;
import greencity.enums.AddressStatus;
import greencity.entity.user.ubs.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddressDtoMapper extends AbstractConverter<AddressDto, Address> {
    @Override
    protected Address convert(AddressDto addressDto) {
        return Address.builder()
            .id(addressDto.getId())
            .address(BaseAddress.builder()
                .regionUk(addressDto.getRegionUk())
                .regionEn(addressDto.getRegionEn())
                .cityUk(addressDto.getCityUk())
                .cityEn(addressDto.getCityEn())
                .streetUk(addressDto.getStreetUk())
                .streetEn(addressDto.getStreetEn())
                .districtUk(addressDto.getDistrictUk())
                .districtEn(addressDto.getDistrictEn())
                .entranceNumber(addressDto.getEntranceNumber())
                .houseCorpus(addressDto.getHouseCorpus())
                .houseNumber(addressDto.getHouseNumber())
                .addressStatus(AddressStatus.NEW)
                .addressComment(addressDto.getAddressComment())
                .actual(addressDto.getActual())
                .build())
            .coordinates(Coordinates.builder()
                .latitude(addressDto.getCoordinates().getLatitude())
                .longitude(addressDto.getCoordinates().getLongitude())
                .build())

            .build();
    }
}
