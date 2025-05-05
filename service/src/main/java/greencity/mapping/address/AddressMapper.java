package greencity.mapping.address;

import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.BaseAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper extends AbstractConverter<OrderAddressExportDetailsDtoUpdate, Address> {
    @Override
    protected Address convert(OrderAddressExportDetailsDtoUpdate orderAddressExportDetailsDtoUpdate) {
        return Address.builder()
            .id(orderAddressExportDetailsDtoUpdate.getId())
            .baseAddress(BaseAddress.builder()
                .addressComment(orderAddressExportDetailsDtoUpdate.getAddressComment())
                .regionUk(orderAddressExportDetailsDtoUpdate.getRegionUk())
                .regionEn(orderAddressExportDetailsDtoUpdate.getRegionEn())
                .districtEn(orderAddressExportDetailsDtoUpdate.getDistrictEn())
                .districtUk(orderAddressExportDetailsDtoUpdate.getDistrictUk())
                .cityEn(orderAddressExportDetailsDtoUpdate.getCityEn())
                .cityUk(orderAddressExportDetailsDtoUpdate.getCityUk())
                .streetUk(orderAddressExportDetailsDtoUpdate.getStreetUk())
                .streetEn(orderAddressExportDetailsDtoUpdate.getStreetEn())
                .houseNumber(orderAddressExportDetailsDtoUpdate.getHouseNumber())
                .houseCorpus(orderAddressExportDetailsDtoUpdate.getHouseCorpus())
                .entranceNumber(orderAddressExportDetailsDtoUpdate.getEntranceNumber())
                .build())
            .build();
    }
}
