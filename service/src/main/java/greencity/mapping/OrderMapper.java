package greencity.mapping;

import greencity.dto.OrderResponseDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.repository.CertificateRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that used by {@link ModelMapper} to map {@link OrderResponseDto} into
 * {@link Order}.
 */
@Component
@AllArgsConstructor
public class OrderMapper extends AbstractConverter<OrderResponseDto, Order> {
    private final CertificateRepository certificateRepository;

    /**
     * Method convert {@link OrderResponseDto} to {@link Order}.
     *
     * @return {@link Order}
     */
    @Override
    protected Order convert(OrderResponseDto dto) {
        Map<Integer, Integer> map = new HashMap<>();
        dto.getBags().forEach(bag -> {
            map.put(bag.getId(), bag.getAmount());
        });

        Set<Certificate> orderCertificates = new HashSet<>();

        dto.getCerfiticates().forEach(c -> {
            orderCertificates.add(certificateRepository.findById(c).get());
        });

        return Order.builder()
            .orderDate(LocalDateTime.now())
            .amountOfBagsOrdered(map)
            .pointsToUse(dto.getPointsToUse())
            .comment(dto.getOrderComment())
            .certificates(orderCertificates)
            .additionalOrders(dto.getAdditionalOrders())
            .build();
    }
}
