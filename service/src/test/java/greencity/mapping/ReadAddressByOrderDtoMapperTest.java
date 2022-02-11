package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.ReadAddressByOrderDto;
import greencity.entity.user.ubs.Address;
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
        Address address = ModelUtils.address();
        ReadAddressByOrderDto expected = ReadAddressByOrderDto.builder()
            .district(address.getDistrict())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .street(address.getStreet())
            .houseNumber(address.getHouseNumber())
            .build();
        ReadAddressByOrderDto actual = readAddressByOrderDtoMapper.convert(ModelUtils.address());

        assertEquals(expected, actual);
    }
}
