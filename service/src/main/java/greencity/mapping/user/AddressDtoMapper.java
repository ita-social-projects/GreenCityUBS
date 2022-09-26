package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.entity.coords.Coordinates;
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
            .region(addressDto.getRegion())
            .regionEn(addressDto.getRegionEn())
            .city(addressDto.getCity())
            .cityEn(addressDto.getCityEn())
            .street(addressDto.getStreet())
            .streetEn(addressDto.getStreetEn())
            .district(addressDto.getDistrict())
            .districtEn(addressDto.getDistrictEn())
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
