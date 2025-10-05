package greencity.service.ubs.calculator;

import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.TariffsInfo;
import java.util.List;

/**
 * Service for calculating order sums and preparing order bags.
 */
public interface BagCalculatorService {
    /**
     * Prepares {@link OrderBag} objects, validates tariff limits, and calculates
     * the total sum to pay.
     *
     * @param orderBagList list to store created order bags
     * @param bags         customer's selected bags
     * @param tariffsInfo  courier tariff info with limits and available bags
     * @return total sum to pay in coins
     */
    long prepareBagsAndCalculateTotal(List<OrderBag> orderBagList, List<BagDto> bags,
        TariffsInfo tariffsInfo);

    /**
     * Calculates total sum of bags in the order (in coins).
     *
     * @param order current order
     * @return sum of bags in coins
     */
    long getBagsSumToPayInCoins(Order order);

    /**
     * Builds a list of bag DTOs for a user based on order data.
     *
     * @param order current order
     * @return list of bag DTOs with amounts and prices
     */
    List<BagForUserDto> bagForUserDtosBuilder(Order order);

    /**
     * Calculates total sum of given bags (in coins).
     *
     * @param bagForUserDtos list of user bag DTOs
     * @return sum in coins
     */
    Long calculateBugsSum(List<BagForUserDto> bagForUserDtos);
}
