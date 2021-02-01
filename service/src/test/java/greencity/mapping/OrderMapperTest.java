package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.OrderResponseDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.repository.CertificateRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {
    @InjectMocks
    private OrderMapper orderMapper;
    @Mock
    private CertificateRepository certificateRepository;

    @Test
    void convert() {
        OrderResponseDto orderResponseDto = ModelUtils.getOrderResponceDto();
        Map<Integer, Integer> orderedBags = new HashMap<>();
        orderedBags.put(3, 999);
        Certificate certificate = new Certificate();
        Order expected = Order.builder()
            .additionalOrder("232-534-634")
            .amountOfBagsOrdered(orderedBags)
            .comment("comment")
            .certificate(certificate)
            .pointsToUse(700)
            .build();

        when(certificateRepository.findById(anyString())).thenReturn(Optional.of(certificate));

        Order actual = orderMapper.convert(orderResponseDto);
        actual.setOrderDate(null);

        assertEquals(expected, actual);
    }
}