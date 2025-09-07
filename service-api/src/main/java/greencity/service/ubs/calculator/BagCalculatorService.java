package greencity.service.ubs.calculator;

import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.TariffsInfo;
import java.util.List;

//TODO add docs
//TODO add tests
public interface BagCalculatorService {
    long formBagsToBeSavedAndCalculateOrderSum(List<OrderBag> orderBagList, List<BagDto> bags,
                                               TariffsInfo tariffsInfo);

    long getBagsSumToPayInCoins(Order order);

    List<BagForUserDto> bagForUserDtosBuilder(Order order);

    Long calculateBugsSum(List<BagForUserDto> bagForUserDtos);
}
