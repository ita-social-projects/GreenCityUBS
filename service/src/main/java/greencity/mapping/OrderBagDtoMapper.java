package greencity.mapping;

import greencity.dto.OrderBagDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OrderBagDtoMapper extends AbstractConverter<Order, List<OrderBagDto>> {
    /**
     * Method convert {@link Order} to {@link OrderBagDto}.
     *
     * @return {@link OrderBagDto}
     */
    @Override
    protected List<OrderBagDto> convert(Order order) {
        List<OrderBagDto> build = new ArrayList<>();
        for (Map.Entry<Integer, Integer> pair : order.getAmountOfBagsOrdered().entrySet()) {
            build.add(OrderBagDto.builder()
                .id(pair.getKey())
                .amount(pair.getValue())
                .build());
        }
        return build;
    }
}
