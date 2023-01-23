package greencity.mapping.order;

import greencity.dto.order.ReadAddressByOrderDto;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Address} into
 * {@link ReadAddressByOrderDto}.
 */
@Component
public class ReadAddressByOrderDtoMapper extends AbstractConverter<OrderAddress, ReadAddressByOrderDto> {
    /**
     * Method convert {@link OrderAddress} to {@link ReadAddressByOrderDto}.
     *
     * @return {@link ReadAddressByOrderDto}
     */
    @Override
    protected ReadAddressByOrderDto convert(OrderAddress address) {
        return ReadAddressByOrderDto.builder()
            .district(address.getDistrict())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .street(address.getStreet())
            .houseNumber(address.getHouseNumber())
            .comment(address.getAddressComment())
            .build();
    }
}
