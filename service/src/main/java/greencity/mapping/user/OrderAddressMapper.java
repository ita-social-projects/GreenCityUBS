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
            .baseAddress(BaseAddress.builder()
                .regionUk(address.getBaseAddress().getRegionUk())
                .cityUk(address.getBaseAddress().getCityUk())
                .streetUk(address.getBaseAddress().getStreetUk())
                .districtUk(address.getBaseAddress().getDistrictUk())
                .houseNumber(address.getBaseAddress().getHouseNumber())
                .houseCorpus(address.getBaseAddress().getHouseCorpus())
                .entranceNumber(address.getBaseAddress().getEntranceNumber())
                .addressComment(address.getBaseAddress().getAddressComment())
                .actual(address.getBaseAddress().getActual())
                .addressStatus(address.getBaseAddress().getAddressStatus())
                .regionEn(address.getBaseAddress().getRegionEn())
                .cityEn(address.getBaseAddress().getCityEn())
                .streetEn(address.getBaseAddress().getStreetEn())
                .districtEn(address.getBaseAddress().getDistrictEn())
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
