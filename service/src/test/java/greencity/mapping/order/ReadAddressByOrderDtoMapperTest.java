package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.entity.user.ubs.OrderAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReadAddressByOrderDtoMapperTest {
    @InjectMocks
    ReadAddressByOrderDtoMapper readAddressByOrderDtoMapper;

    @Test
    void convert() {
        OrderAddress address = ModelUtils.getOrderAddress();
        ReadAddressByOrderDto expected = ReadAddressByOrderDto.builder()
            .district(address.getAddress().getDistrictUk())
            .entranceNumber(address.getAddress().getEntranceNumber())
            .houseCorpus(address.getAddress().getHouseCorpus())
            .street(address.getAddress().getStreetUk())
            .houseNumber(address.getAddress().getHouseNumber())
            .comment(address.getAddress().getAddressComment())
            .build();
        ReadAddressByOrderDto actual = readAddressByOrderDtoMapper.convert(ModelUtils.getOrderAddress());

        assertEquals(expected, actual);
    }
}
