package greencity.mapping;

import greencity.dto.AddressDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.AddressStatus;
import greencity.entity.user.ubs.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddressDtoMapper extends AbstractConverter<AddressDto, Address> {
    @Override
    protected Address convert(AddressDto addressDto) {
        return Address.builder()
            .id(addressDto.getId())
            .region(addressDto.getRegion())
            .city(addressDto.getCity())
            .street(addressDto.getStreet())
            .district(addressDto.getDistrict())
            .entranceNumber(addressDto.getEntranceNumber())
            .houseCorpus(addressDto.getHouseCorpus())
            .houseNumber(addressDto.getHouseNumber())
            .addressStatus(AddressStatus.NEW)
            .addressComment(addressDto.getAddressComment())
            .coordinates(Coordinates.builder()
                .latitude(addressDto.getCoordinates().getLatitude())
                .longitude(addressDto.getCoordinates().getLongitude())
                .build())
            .actual(false)
            .build();
    }
}
