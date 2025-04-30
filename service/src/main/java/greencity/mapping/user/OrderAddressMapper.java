package greencity.mapping.user;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressMapper extends AbstractConverter<Address, OrderAddress> {
    @Override
    protected OrderAddress convert(Address address) {
        return OrderAddress.builder()
            .region(address.getRegion())
            .city(address.getCity())
            .street(address.getStreet())
            .district(address.getDistrict())
            .houseNumber(address.getHouseNumber())
            .houseCorpus(address.getHouseCorpus())
            .entranceNumber(address.getEntranceNumber())
            .addressComment(address.getAddressComment())
            .actual(address.getActual())
            .addressStatus(address.getAddressStatus())
            .coordinates(address.getCoordinates() != null
                ? Coordinates.builder()
                    .latitude(address.getCoordinates().getLatitude())
                    .longitude(address.getCoordinates().getLongitude())
                    .build()
                : null)
            .regionEn(address.getRegionEn())
            .cityEn(address.getCityEn())
            .streetEn(address.getStreetEn())
            .districtEn(address.getDistrictEn())
            .districtId(address.getDistrictId())
            .cityId(address.getCityId())
            .regionId(address.getRegionId())
            .build();
    }
}
