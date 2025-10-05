package greencity.service.ubs.calculator;

import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.NOT_ENOUGH_BAGS_EXCEPTION;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_LOWER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.TOO_MANY_BAGS_EXCEPTION;
import greencity.constant.AppConstant;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.TariffsInfo;
import greencity.enums.BagStatus;
import greencity.enums.CourierLimit;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.BagRepository;
import greencity.service.ubs.OrderBagService;
import greencity.util.MoneyConverterUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BagCalculatorServiceImpl implements BagCalculatorService {
    private final BagRepository bagRepository;
    private final MoneyConverterUtil moneyConverterUtil;
    private final ModelMapper modelMapper;
    private final OrderBagService orderBagService;

    @Override
    public long prepareBagsAndCalculateTotal(List<OrderBag> orderBagList, List<BagDto> bags,
        TariffsInfo tariffsInfo) {
        CalculationContext context = calculateOrderedBags(orderBagList, bags);

        validateLimits(tariffsInfo, context);

        addNotOrderedBugs(orderBagList, tariffsInfo, context.bagIds);

        return context.totalSumToPayInCoins;
    }

    @Override
    public long getBagsSumToPayInCoins(Order order) {
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        return calculateBugsSum(bagForUserDtos);
    }

    @Override
    public List<BagForUserDto> bagForUserDtosBuilder(Order order) {
        List<OrderBag> bagsAmountInOrder = order.getOrderBags();
        Map<Integer, Integer> actualBagsAmount = orderBagService.getActualBagsAmountForOrder(bagsAmountInOrder);
        return bagsAmountInOrder.stream()
            .map(orderBag -> buildBagForUserDto(orderBag, actualBagsAmount.get(orderBag.getBag().getId())))
            .toList();
    }

    @Override
    public Long calculateBugsSum(List<BagForUserDto> bagForUserDtos) {
        return bagForUserDtos.stream()
            .map(b -> moneyConverterUtil.convertBillsIntoCoins(b.getTotalPrice()))
            .reduce(0L, Long::sum);
    }

    private void addNotOrderedBugs(List<OrderBag> orderBagList, TariffsInfo tariffsInfo, List<Integer> bagIds) {
        List<OrderBag> notOrderedBags = tariffsInfo.getBags().stream()
            .filter(orderBag -> orderBag.getStatus() == BagStatus.ACTIVE && !bagIds.contains(orderBag.getId()))
            .map(this::createOrderBag)
            .toList();
        orderBagList.addAll(notOrderedBags.stream()
            .map(this::setAmountToOrderBag)
            .toList());
    }

    private void validateLimits(TariffsInfo tariffsInfo, CalculationContext context) {
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, context.limitedSumToPayInCoins);
        checkAmountOfBagsIfCourierLimitByAmountOfBag(tariffsInfo, context.limitedBags);
    }

    private CalculationContext calculateOrderedBags(List<OrderBag> orderBagList, List<BagDto> bags) {
        long totalSum = 0L;
        long limitedSum = 0L;
        int limitedBags = 0;
        List<Integer> bagIds = new ArrayList<>();

        for (BagDto dto : bags) {
            Bag bag = findActiveBagById(dto.getId());
            bagIds.add(dto.getId());

            if (Boolean.TRUE.equals(bag.getLimitIncluded())) {
                limitedSum += bag.getFullPrice() * dto.getAmount();
                limitedBags += dto.getAmount();
            } else {
                totalSum += bag.getFullPrice() * dto.getAmount();
            }

            OrderBag orderBag = createOrderBag(bag);
            orderBag.setAmount(dto.getAmount());
            orderBagList.add(orderBag);
        }

        totalSum += limitedSum;
        return new CalculationContext(totalSum, limitedSum, limitedBags, bagIds);
    }

    private OrderBag createOrderBag(Bag bag) {
        return OrderBag.builder()
            .bag(bag)
            .capacity(bag.getCapacity())
            .price(bag.getFullPrice())
            .nameUk(bag.getNameUk())
            .nameEn(bag.getNameEn())
            .build();
    }

    private Bag findActiveBagById(Integer id) {
        return bagRepository.findActiveBagById(id)
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + id));
    }

    private void checkSumIfCourierLimitBySumOfOrder(TariffsInfo tariffsInfo, Long sumWithoutDiscountInCoins) {
        if (!CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(tariffsInfo.getCourierLimit())) {
            return;
        }

        long sum = sumWithoutDiscountInCoins;
        Long min = tariffsInfo.getMin() != null ? tariffsInfo.getMin() * AppConstant.CURRENCY_CONVERSION_RATE : null;
        Long max = tariffsInfo.getMax() != null ? tariffsInfo.getMax() * AppConstant.CURRENCY_CONVERSION_RATE : null;

        if (min != null && sum < min) {
            throw new BadRequestException(PRICE_OF_ORDER_LOWER_THAN_LIMIT + tariffsInfo.getMin());
        }
        if (max != null && sum > max) {
            throw new BadRequestException(PRICE_OF_ORDER_GREATER_THAN_LIMIT + tariffsInfo.getMax());
        }
    }

    private void checkAmountOfBagsIfCourierLimitByAmountOfBag(TariffsInfo courierLocation, Integer countOfBigBag) {
        if (!CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())) {
            return;
        }

        Long min = courierLocation.getMin();
        Long max = courierLocation.getMax();

        if (min != null && countOfBigBag < min) {
            throw new BadRequestException(NOT_ENOUGH_BAGS_EXCEPTION + min);
        }

        if (max != null && countOfBigBag > max) {
            throw new BadRequestException(TOO_MANY_BAGS_EXCEPTION + max);
        }
    }

    private OrderBag setAmountToOrderBag(OrderBag orderBag) {
        orderBag.setAmount(0);
        return orderBag;
    }

    private BagForUserDto buildBagForUserDto(OrderBag orderBag, int count) {
        BagForUserDto bagDto = modelMapper.map(orderBag, BagForUserDto.class);
        bagDto.setCount(count);
        bagDto.setTotalPrice(moneyConverterUtil.convertCoinsIntoBills(count * orderBag.getPrice()));
        return bagDto;
    }

    private record CalculationContext(
        long totalSumToPayInCoins,
        long limitedSumToPayInCoins,
        int limitedBags,
        List<Integer> bagIds) {
    }
}
