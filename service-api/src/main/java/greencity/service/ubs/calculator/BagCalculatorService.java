package greencity.service.ubs.calculator;

import greencity.dto.bag.BagDto;
import greencity.entity.order.Bag;
import greencity.entity.order.OrderBag;
import greencity.entity.order.TariffsInfo;
import java.util.List;

public interface BagCalculatorService {
    long formBagsToBeSavedAndCalculateOrderSum(List<OrderBag> orderBagList, List<BagDto> bags,
                                               TariffsInfo tariffsInfo);

    OrderBag createOrderBag(Bag bag);
}
