package greencity.service.ubs;

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
import java.util.Map;
import java.util.HashMap;
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
    public List<Bag> findAllBagsInOrderBagsList(List<OrderBag> orderBags) {
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
     * @param  id The ID of the OrderBag to search for.
     * @return A list of Bag instances associated with the provided OrderBag ID.
     */
    public List<Bag> findBagsByOrderId(Long id) {
        List<OrderBag> orderBags = orderBagRepository.findOrderBagsByOrderId(id);
        return findAllBagsInOrderBagsList(orderBags);
    }

    public Map<Integer, Integer> getActualBagsAmountForOrder(List<OrderBag> bagsForOrder) {
        if (bagsForOrder.stream().allMatch(it -> it.getExportedQuantity() != null)) {
            return bagsForOrder.stream()
                .collect(Collectors.toMap(it -> it.getBag().getId(), OrderBag::getExportedQuantity));
        }
        if (bagsForOrder.stream().allMatch(it -> it.getConfirmedQuantity() != null)) {
            return bagsForOrder.stream()
                .collect(Collectors.toMap(it -> it.getBag().getId(), OrderBag::getConfirmedQuantity));
        }
        if (bagsForOrder.stream().allMatch(it -> it.getAmount() != null)) {
            return bagsForOrder.stream()
                .collect(Collectors.toMap(it -> it.getBag().getId(), OrderBag::getAmount));
        }
        return new HashMap<>();
    }
}
