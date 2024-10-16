package greencity.service.ubs;

import greencity.entity.order.Bag;
import greencity.entity.order.OrderBag;
import greencity.exceptions.NotFoundException;
import greencity.repository.OrderBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderBagService {
    private final OrderBagRepository orderBagRepository;

    private Long getBagPrice(List<OrderBag> orderBags, Integer id) {
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
            .peek(b -> b.setFullPrice(getBagPrice(orderBags, b.getId())))
            .collect(Collectors.toList());
    }

    /**
     * Finds all bags belonging to a specific OrderBag based on the provided ID.
     *
     * @param id The ID of the OrderBag to search for.
     * @return A list of Bag instances associated with the provided OrderBag ID.
     */
    public List<Bag> findAllBagsByOrderId(Long id) {
        List<OrderBag> orderBags = orderBagRepository.findOrderBagsByOrderId(id);
        return findAllBagsInOrderBagsList(orderBags);
    }

    /**
     * Calculates the actual bags' amounts for the given list of OrderBags and
     * returns the result as a Map. This method checks the OrderBags in the input
     * list and calculates the actual amount for each bag based on the availability
     * of different quantity attributes in the OrderBag objects. It prioritizes the
     * following quantities in descending order: 1. Exported Quantity: If all
     * OrderBags have the 'exportedQuantity' attribute set, the method will use it.
     * 2. Confirmed Quantity: If 'exportedQuantity' is not available for all
     * OrderBags but 'confirmedQuantity' is, the method will use it. 3. Regular
     * Amount: If neither 'exportedQuantity' nor 'confirmedQuantity' are available
     * for all OrderBags, the method will use the 'amount' attribute.
     *
     * @param bagsForOrder The list of OrderBag objects for which the actual amounts
     *                     need to be calculated.
     * @return A Map containing the bag ID as the key and the corresponding actual
     *         amount as the value. If any OrderBag in the input list lacks all
     *         three attributes (exportedQuantity, confirmedQuantity, and amount),
     *         the corresponding entry will not be included in the result map.
     * @throws NullPointerException if 'bagsForOrder' is null.
     */
    public Map<Integer, Integer> getActualBagsAmountForOrder(List<OrderBag> bagsForOrder) {
        if (bagsForOrder.stream().allMatch(orderBag -> orderBag.getExportedQuantity() != null)) {
            return bagsForOrder.stream()
                .collect(Collectors.toMap(orderBag -> orderBag.getBag().getId(), OrderBag::getExportedQuantity));
        }
        if (bagsForOrder.stream().allMatch(orderBag -> orderBag.getConfirmedQuantity() != null)) {
            return bagsForOrder.stream()
                .collect(Collectors.toMap(orderBag -> orderBag.getBag().getId(), OrderBag::getConfirmedQuantity));
        }
        if (bagsForOrder.stream().allMatch(orderBag -> orderBag.getAmount() != null)) {
            return bagsForOrder.stream()
                .collect(Collectors.toMap(orderBag -> orderBag.getBag().getId(), OrderBag::getAmount));
        }
        return new HashMap<>();
    }
}
