package greencity.service.ubs.calculator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class BagCalculatorServiceImplTest {
    @Mock
    private MoneyConverterUtil moneyConverterUtil;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private OrderBagService orderBagService;
    @InjectMocks
    private BagCalculatorServiceImpl bagCalculatorService;

    @Test
    void prepareBagsAndCalculateTotal_shouldCalculateTotal() {
        List<OrderBag> orderBagList = new ArrayList<>();
        List<BagDto> bags = List.of(ModelUtils.getBagDto());
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));

        long total = bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, bags, tariffsInfo);

        assertTrue(total > 0);
        assertFalse(orderBagList.isEmpty());
        verify(bagRepository).findActiveBagById(anyInt());
    }

    @Test
    void getBagsSumToPayInCoins_shouldReturnSum() {
        OrderBag orderBag1 = ModelUtils.getOrderBag();
        OrderBag orderBag2 = ModelUtils.getOrderBag();
        orderBag2.setId(2L);
        orderBag2.getBag().setId(2);
        BagForUserDto bagDto = ModelUtils.getBagForUserDto();
        BagForUserDto bagDto2 = ModelUtils.getBagForUserDto();

        Order order = orderBag1.getOrder();
        order.setOrderBags(List.of(orderBag1, orderBag2));

        when(orderBagService.getActualBagsAmountForOrder(order.getOrderBags()))
            .thenReturn(Map.of(
                orderBag1.getBag().getId(), 1,
                orderBag2.getBag().getId(), 2));

        when(modelMapper.map(orderBag1, BagForUserDto.class))
            .thenReturn(bagDto);
        when(modelMapper.map(orderBag2, BagForUserDto.class))
            .thenReturn(bagDto2);

        when(moneyConverterUtil.convertCoinsIntoBills(orderBag1.getPrice() * 1))
            .thenReturn(10.0);
        when(moneyConverterUtil.convertCoinsIntoBills(orderBag2.getPrice() * 2))
            .thenReturn(20.0);

        when(moneyConverterUtil.convertBillsIntoCoins(10.0)).thenReturn(orderBag1.getPrice() * 1);
        when(moneyConverterUtil.convertBillsIntoCoins(20.0)).thenReturn(orderBag2.getPrice() * 2);

        long sum = bagCalculatorService.getBagsSumToPayInCoins(order);
        long expectedSum = orderBag1.getPrice() * 1 + orderBag2.getPrice() * 2;

        assertEquals(expectedSum, sum);
    }

    @Test
    void bagForUserDtosBuilder_shouldBuildDtos() {
        OrderBag orderBag = ModelUtils.getOrderBag();
        Order order = orderBag.getOrder();
        List<OrderBag> orderBags = List.of(orderBag);
        order.setOrderBags(orderBags);
        BagForUserDto bagDto = ModelUtils.getBagForUserDto();

        when(orderBagService.getActualBagsAmountForOrder(orderBags))
            .thenReturn(Map.of(orderBag.getBag().getId(), 2));
        when(modelMapper.map(orderBags.getFirst(), BagForUserDto.class))
            .thenReturn(bagDto);
        when(moneyConverterUtil.convertCoinsIntoBills(anyLong())).thenReturn(2.0);

        List<BagForUserDto> dtos = bagCalculatorService.bagForUserDtosBuilder(order);

        assertEquals(1, dtos.size());
        assertEquals(2.0, dtos.get(0).getTotalPrice());
    }

    @Test
    void calculateBagsSum_shouldSumAll() {
        List<BagForUserDto> dtos = List.of(
            BagForUserDto.builder().totalPrice(10.0).build(),
            BagForUserDto.builder().totalPrice(5.0).build());
        when(moneyConverterUtil.convertBillsIntoCoins(10.0)).thenReturn(1000L);
        when(moneyConverterUtil.convertBillsIntoCoins(5.0)).thenReturn(500L);

        Long sum = bagCalculatorService.calculateBugsSum(dtos);

        assertEquals(1500L, sum);
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenBagNotFound() {
        List<OrderBag> orderBagList = new ArrayList<>();
        List<BagDto> bags = List.of(ModelUtils.getBagDto());
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(bagRepository.findActiveBagById(anyInt()))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, bags, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenSumLowerThanMin() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(1);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(1000L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenSumGreaterThanMax() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(100);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMax(1L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenBagsLessThanMin() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(1);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMin(10L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenBagsGreaterThanMax() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(100);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMax(1L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldAddNotOrderedBags() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Bag activeBag = ModelUtils.getBaglist().get(0);
        activeBag.setId(999);
        activeBag.setStatus(BagStatus.ACTIVE);
        tariffsInfo.setBags(List.of(activeBag));

        when(bagRepository.findActiveBagById(anyInt()))
            .thenReturn(Optional.of(ModelUtils.getBaglist().get(0)));

        long total = bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, List.of(dto), tariffsInfo);

        assertFalse(orderBagList.isEmpty());
        assertTrue(orderBagList.stream().anyMatch(b -> b.getBag().getId().equals(999) && b.getAmount() == 0));
        assertTrue(total > 0);
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenAmountLimitNotMet() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(2);

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(false);

        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldPassWhenMinMaxNull() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(null);
        tariffsInfo.setMax(null);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);

        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertDoesNotThrow(
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldPassWhenBagsWithinMinMax() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(2);

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMin(1L);
        tariffsInfo.setMax(3L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);

        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertDoesNotThrow(
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldIgnoreCheckWhenMinNull() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(0);

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMin(null);
        tariffsInfo.setMax(5L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);

        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));
        List<BagDto> dtoList = List.of(dto);

        assertDoesNotThrow(
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldNotAddInactiveBags() {
        List<OrderBag> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Bag inactiveBag = ModelUtils.getBaglist().get(0);
        inactiveBag.setId(123);
        inactiveBag.setStatus(BagStatus.DELETED);
        tariffsInfo.setBags(List.of(inactiveBag));

        when(bagRepository.findActiveBagById(anyInt()))
            .thenReturn(Optional.of(ModelUtils.getBaglist().get(0)));

        long total = bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, List.of(dto), tariffsInfo);

        assertTrue(orderBagList.stream().noneMatch(b -> b.getBag().getId().equals(123)));
        assertTrue(total > 0);
    }
}