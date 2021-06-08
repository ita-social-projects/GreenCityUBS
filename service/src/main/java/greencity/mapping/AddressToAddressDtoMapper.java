package greencity.mapping;

import greencity.dto.AddressDto;
import greencity.entity.user.ubs.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddressToAddressDtoMapper extends AbstractConverter<Address, AddressDto> {
    @Override
    protected AddressDto convert(Address address) {
        return AddressDto.builder()
            .id(address.getId())
            .city(address.getCity())
            .district(address.getDistrict())
            .street(address.getStreet())
            .houseCorpus(address.getHouseCorpus())
            .entranceNumber(address.getEntranceNumber())
            .houseNumber(address.getHouseNumber())
            .actual(address.getActual())
            .coordinates(address.getCoordinates())
            .build();
    }
}
