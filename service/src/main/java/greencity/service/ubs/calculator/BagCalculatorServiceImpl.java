package greencity.service.ubs.calculator;

import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.NOT_ENOUGH_BAGS_EXCEPTION;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_LOWER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.TOO_MANY_BAGS_EXCEPTION;
import greencity.constant.AppConstant;
import greencity.dto.bag.BagDto;
import greencity.entity.order.Bag;
import greencity.entity.order.OrderBag;
import greencity.entity.order.TariffsInfo;
import greencity.enums.BagStatus;
import greencity.enums.CourierLimit;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.BagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BagCalculatorServiceImpl implements BagCalculatorService {
    private final BagRepository bagRepository;

    @Override
    public long formBagsToBeSavedAndCalculateOrderSum(List<OrderBag> orderBagList, List<BagDto> bags,
                                                      TariffsInfo tariffsInfo) {
        long totalSumToPayInCoins = 0L;
        long limitedSumToPayInCoins = 0L;
        int limitedBags = 0;
        final List<Integer> bagIds = bags.stream().map(BagDto::getId).toList();
        for (BagDto temp : bags) {
            Bag bag = findActiveBagById(temp.getId());
            if (Boolean.TRUE.equals(bag.getLimitIncluded())) {
                limitedSumToPayInCoins += bag.getFullPrice() * temp.getAmount();
                limitedBags += temp.getAmount();
            } else {
                totalSumToPayInCoins += bag.getFullPrice() * temp.getAmount();
            }
            OrderBag orderBag = createOrderBag(bag);
            orderBag.setAmount(temp.getAmount());
            orderBagList.add(orderBag);
        }
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, limitedSumToPayInCoins);
        checkAmountOfBagsIfCourierLimitByAmountOfBag(tariffsInfo, limitedBags);
        totalSumToPayInCoins += limitedSumToPayInCoins;
        List<OrderBag> notOrderedBags = tariffsInfo.getBags().stream()
            .filter(orderBag -> orderBag.getStatus() == BagStatus.ACTIVE && !bagIds.contains(orderBag.getId()))
            .map(this::createOrderBag)
            .toList();
        orderBagList.addAll(notOrderedBags.stream()
            .map(this::setAmountToOrderBag)
            .toList());
        return totalSumToPayInCoins;
    }

    private Bag findActiveBagById(Integer id) {
        return bagRepository.findActiveBagById(id)
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + id));
    }

    private void checkSumIfCourierLimitBySumOfOrder(TariffsInfo tariffsInfo, Long sumWithoutDiscountInCoins) {
        if (CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(tariffsInfo.getCourierLimit())) {
            if (sumWithoutDiscountInCoins < tariffsInfo.getMin() * AppConstant.CURRENCY_CONVERSION_RATE) {
                throw new BadRequestException(PRICE_OF_ORDER_LOWER_THAN_LIMIT + tariffsInfo.getMin());
            }
            if (tariffsInfo.getMax() != null
                && sumWithoutDiscountInCoins > tariffsInfo.getMax() * AppConstant.CURRENCY_CONVERSION_RATE) {
                throw new BadRequestException(PRICE_OF_ORDER_GREATER_THAN_LIMIT + tariffsInfo.getMax());
            }
        }
    }

    private void checkAmountOfBagsIfCourierLimitByAmountOfBag(TariffsInfo courierLocation, Integer countOfBigBag) {
        if (CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())) {
            if (courierLocation.getMin() > countOfBigBag) {
                throw new BadRequestException(NOT_ENOUGH_BAGS_EXCEPTION + courierLocation.getMin());
            }
            if (courierLocation.getMax() != null && courierLocation.getMax() < countOfBigBag) {
                throw new BadRequestException(TOO_MANY_BAGS_EXCEPTION + courierLocation.getMax());
            }
        }
    }

    private OrderBag setAmountToOrderBag(OrderBag orderBag) {
        orderBag.setAmount(0);
        return orderBag;
    }

    @Override
    public OrderBag createOrderBag(Bag bag) {
        return OrderBag.builder()
            .bag(bag)
            .capacity(bag.getCapacity())
            .price(bag.getFullPrice())
            .nameUk(bag.getNameUk())
            .nameEn(bag.getNameEn())
            .build();
    }
}
