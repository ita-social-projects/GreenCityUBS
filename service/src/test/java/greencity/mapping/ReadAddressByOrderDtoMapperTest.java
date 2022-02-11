package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.ReadAddressByOrderDto;
import greencity.entity.user.ubs.Address;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReadAddressByOrderDtoMapperTest {

    @InjectMocks
    ReadAddressByOrderDtoMapper readAddressByOrderDtoMapper;

    @Test
    void convert() {

        Address address = ModelUtils.address();

        ReadAddressByOrderDto expected = ReadAddressByOrderDto.builder()
            .district("Zaliznuchnuy")
            .entranceNumber("7a")
            .houseCorpus("2")
            .street("Gorodotska")
            .comment(null)
            .houseNumber("7")
            .build();

        ReadAddressByOrderDto actual = readAddressByOrderDtoMapper.convert(address);

        Assertions.assertEquals(actual.getComment(), expected.getComment());
        Assertions.assertEquals(actual.getDistrict(), expected.getDistrict());
        Assertions.assertEquals(actual.getEntranceNumber(), expected.getEntranceNumber());
        Assertions.assertEquals(actual.getHouseCorpus(), expected.getHouseCorpus());
        Assertions.assertEquals(actual.getStreet(), expected.getStreet());
        Assertions.assertEquals(actual.getHouseNumber(), expected.getHouseNumber());

    }

}
