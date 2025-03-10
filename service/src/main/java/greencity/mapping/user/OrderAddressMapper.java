package greencity.mapping.user;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.BaseAddress;
import greencity.entity.user.ubs.OrderAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressMapper extends AbstractConverter<Address, OrderAddress> {
    @Override
    protected OrderAddress convert(Address address) {
        return OrderAddress.builder()
            .address(BaseAddress.builder()
                .regionUk(address.getAddress().getRegionUk())
                .cityUk(address.getAddress().getCityUk())
                .streetUk(address.getAddress().getStreetUk())
                .districtUk(address.getAddress().getDistrictUk())
                .houseNumber(address.getAddress().getHouseNumber())
                .houseCorpus(address.getAddress().getHouseCorpus())
                .entranceNumber(address.getAddress().getEntranceNumber())
                .addressComment(address.getAddress().getAddressComment())
                .actual(address.getAddress().getActual())
                .addressStatus(address.getAddress().getAddressStatus())
                .regionEn(address.getAddress().getRegionEn())
                .cityEn(address.getAddress().getCityEn())
                .streetEn(address.getAddress().getStreetEn())
                .districtEn(address.getAddress().getDistrictEn())
                .build())
            .coordinates(address.getCoordinates() != null
                ? Coordinates.builder()
                    .latitude(address.getCoordinates().getLatitude())
                    .longitude(address.getCoordinates().getLongitude())
                    .build()
                : null)

            .districtId(address.getDistrictId())
            .cityId(address.getCityId())
            .regionId(address.getRegionId())
            .build();
    }
}
