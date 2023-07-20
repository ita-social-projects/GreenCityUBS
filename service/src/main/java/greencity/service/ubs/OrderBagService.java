package greencity.service.ubs;

import greencity.entity.order.Bag;
import greencity.entity.order.OrderBag;
import greencity.exceptions.NotFoundException;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;

@Service
@AllArgsConstructor
@Slf4j
public class OrderBagService {
    private final OrderRepository orderRepository;
    private final OrderBagRepository orderBagRepository;

    private Long getActualPrice(List<OrderBag> orderBags, Integer id) {
        return orderBags.stream()
            .filter(ob -> ob.getBag().getId().equals(id))
            .map(OrderBag::getPrice)
            .findFirst()
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + id));
    }

    public List<Bag> findBagsByOrderId(List<OrderBag> orderBags) {
        return orderBags.stream()
            .map(OrderBag::getBag)
            .peek(b -> b.setFullPrice(getActualPrice(orderBags, b.getId())))
            .collect(Collectors.toList());
    }

    public List<Bag> findBagsByOrderId(Long id) {
        List<OrderBag> orderBags = orderBagRepository.findAll();
        return orderBags.stream()
            .filter(ob -> ob.getBag().getId().equals(id))
            .map(OrderBag::getBag)
            .peek(b -> b.setFullPrice(getActualPrice(orderBags, b.getId())))
            .collect(Collectors.toList());
    }
}
