package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.OrderAddressDtoResponse;
import greencity.entity.user.ubs.Address;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderAddressDtoUpdateMapperTest {

    @InjectMocks
    OrderAddressDtoUpdateMapper orderAddressDtoUpdateMapper;

    @Test
    void convert() {

        Address address = ModelUtils.address();

        OrderAddressDtoResponse expected = OrderAddressDtoResponse.builder()
            .district("Zaliznuchnuy")
            .street("Gorodotska")
            .houseCorpus("2")
            .entranceNumber("7a")
            .houseNumber("7")
            .build();

        OrderAddressDtoResponse actual = orderAddressDtoUpdateMapper.convert(address);

        Assertions.assertEquals(actual.toString(), expected.toString());

    }
}
