package greencity.mapping;

import greencity.dao.entity.order.Order;
import greencity.dao.repository.CertificateRepository;
import greencity.dto.OrderResponseDto;
import org.modelmapper.AbstractConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderMapper extends AbstractConverter<OrderResponseDto, Order> {
    private final CertificateRepository certificateRepository;

    @Autowired
    public OrderMapper(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    protected Order convert(OrderResponseDto dto) {
        Map<Integer, Integer> map = new HashMap<>();
        dto.getBags().forEach(bag -> {
            map.put(bag.getId(), bag.getAmount());
        });

        return Order.builder()
                .orderDate(LocalDateTime.now())
                .amountOfBagsOrdered(map)
                .pointsToUse(dto.getPointsToUse())
                .comment(dto.getOrderComment())
                .additionalOrder(dto.getAdditionalOrder())
                .certificate(certificateRepository.findById(dto.getCerfiticate()).orElse(null))
                .build();
    }

}
