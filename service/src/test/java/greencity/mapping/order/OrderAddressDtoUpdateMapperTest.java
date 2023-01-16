package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.entity.user.ubs.Address;
import greencity.mapping.order.OrderAddressDtoUpdateMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderAddressDtoUpdateMapperTest {

    @InjectMocks
    OrderAddressDtoUpdateMapper orderAddressDtoUpdateMapper;

    @Test
    void convert() {

        Address address = ModelUtils.getAddress();

        OrderAddressDtoResponse expected = OrderAddressDtoResponse.builder()
            .district("Distinct")
            .street("Street")
            .houseCorpus("2")
            .entranceNumber("7a")
            .houseNumber("25")
            .build();

        OrderAddressDtoResponse actual = orderAddressDtoUpdateMapper.convert(address);

        Assertions.assertEquals(actual.toString(), expected.toString());

    }
}
