package greencity.service.ubs.calculator;

import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.order.OrderInfoDto;
import java.util.List;

/**
 * Service for calculating order sums and preparing order bags.
 */
public interface BagCalculatorService {
    /**
     * Prepares {@link BagDto} objects, validates tariff limits, and calculates the
     * total sum to pay.
     *
     * @param orderBagList  list to store created order bags
     * @param bags          customer's selected bags
     * @param tariffsInfoId courier tariff info id
     * @return total sum to pay in coins
     */
    long prepareBagsAndCalculateTotal(List<BagInfoDto> orderBagList, List<BagDto> bags, Long tariffsInfoId);

    /**
     * Calculates total sum of bags in the order (in coins).
     *
     * @param orderInfo current order info
     * @return sum of bags in coins
     */
    long getBagsSumToPayInCoins(OrderInfoDto orderInfo);

    /**
     * Builds a list of bag DTOs for a user based on order data.
     *
     * @param orderInfo current order info
     * @return list of bag DTOs with amounts and prices
     */
    List<BagForUserDto> bagForUserDtosBuilder(OrderInfoDto orderInfo);

    /**
     * Calculates total sum of given bags (in coins).
     *
     * @param bagForUserDtos list of user bag DTOs
     * @return sum in coins
     */
    Long calculateBagsSum(List<BagForUserDto> bagForUserDtos);
}
