package greencity.mapping;

import greencity.dto.ReadAddressByOrderDto;
import greencity.entity.user.ubs.Address;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Address} into
 * {@link ReadAddressByOrderDto}.
 */
@Component
public class ReadAddressByOrderDtoMapper extends AbstractConverter<Address, ReadAddressByOrderDto> {
    /**
     * Method convert {@link Address} to {@link ReadAddressByOrderDto}.
     *
     * @return {@link ReadAddressByOrderDto}
     */
    @Override
    protected ReadAddressByOrderDto convert(Address address) {
        return ReadAddressByOrderDto.builder()
            .district(address.getDistrict())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .street(address.getStreet())
            .houseNumber(address.getHouseNumber())
            .comment(address.getComment())
            .build();
    }
}
