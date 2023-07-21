package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.exceptions.NotFoundException;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;

@Service
@AllArgsConstructor
@Slf4j
public class OrderBagService {
    private final OrderBagRepository orderBagRepository;
    private final OrderRepository orderRepository;

    private Long getActualPrice(List<OrderBag> orderBags, Integer id) {
        return orderBags.stream()
            .filter(ob -> ob.getBag().getId().equals(id))
            .map(OrderBag::getPrice)
            .findFirst()
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + id));
    }

    /**
     * Finds all bags belonging to a specific list of OrderBag instances.
     *
     * @param orderBags A list of OrderBag instances to search within.
     * @return A list of Bag instances associated with the provided OrderBag
     *         instances.
     */
    public List<Bag> findAllBagsInOrderBags(List<OrderBag> orderBags) {
        return orderBags.stream()
            .map(OrderBag::getBag)
            .map(b -> {
                b.setFullPrice(getActualPrice(orderBags, b.getId()));
                return b;
            })
            .collect(Collectors.toList());
    }

    /**
     * Finds all bags belonging to a specific OrderBag based on the provided ID.
     *
     * @param id The ID of the OrderBag to search for.
     * @return A list of Bag instances associated with the provided OrderBag ID.
     */
    public List<Bag> findBagsByOrderId(Long id) {
        List<OrderBag> orderBags = orderBagRepository.findAll();
        return orderBags.stream()
            .filter(ob -> ob.getOrder().getId().equals(id))
            .map(OrderBag::getBag)
            .map(b -> {
                b.setFullPrice(getActualPrice(orderBags, b.getId()));
                return b;
            })
            .collect(Collectors.toList());
    }

    /**
     * method helps to delete bag from order.
     *
     * @param orderBag {@link OrderBag}
     */
    public void removeBagFromOrder(OrderBag orderBag) {
        Order order = orderRepository.findById(orderBag.getOrder().getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.ORDER_NOT_FOUND + orderBag.getOrder().getId()));
        List<OrderBag> modifiableList = new ArrayList<>(order.getOrderBags());
        modifiableList.remove(orderBag);
        order.setOrderBags(modifiableList);
    }
}
