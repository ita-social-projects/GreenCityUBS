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
            .district(address.getDistrict())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .street(address.getStreet())
            .houseNumber(address.getHouseNumber())
            .comment(address.getAddressComment())
            .build();
        ReadAddressByOrderDto actual = readAddressByOrderDtoMapper.convert(ModelUtils.getOrderAddress());

        assertEquals(expected, actual);
    }
}
