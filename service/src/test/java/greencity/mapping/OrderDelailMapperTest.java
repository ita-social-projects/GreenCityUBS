package greencity.mapping;

import greencity.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class OrderDelailMapperTest {

    @InjectMocks
    OrderDelailMapper orderDelailMapper;

    @Test
    void convert() {

        OrderDetailDto orderDetailDto = OrderDetailDto.builder()
            .amount(List.of(BagMappingDto.builder().amount(2).confirmed(2).exported(2).build()))
            .orderId(1L)
            .capacityAndPrice(List.of(BagInfoDto.builder().price(500).capacity(100).name("BigOne").id(1).build()))
            .name(List.of(BagTransDto.builder().name("BigOne").build()))
            .build();

        List<OrderDetailInfoDto> expected = List.of(
            OrderDetailInfoDto.builder()
                .amount(2)
                .bagId(1)
                .capacity(100)
                .confirmedQuantity(2)
                .exportedQuantity(2)
                .name("BigOne")
                .orderId(1L)
                .price(500)
                .build());

        List<OrderDetailInfoDto> actual = orderDelailMapper.convert(orderDetailDto);

        Assertions.assertEquals(expected, actual);

    }
}
