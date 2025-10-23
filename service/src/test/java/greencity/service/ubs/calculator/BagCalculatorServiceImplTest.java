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
import greencity.dto.bag.BagInfoDto;
import greencity.dto.order.OrderInfoDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.TariffsInfo;
import greencity.enums.BagStatus;
import greencity.enums.CourierLimit;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TariffsInfoRepository;
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
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private BagCalculatorServiceImpl bagCalculatorService;

    @Test
    void prepareBagsAndCalculateTotal_shouldCalculateTotal() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        List<BagDto> bags = List.of(ModelUtils.getBagDto());
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        Bag bag1 = ModelUtils.getBaglist().get(0);
        bag1.setLimitIncluded(true);
        BagInfoDto bagDto1 = ModelUtils.getBagInfoDto();
        Bag bag2 = ModelUtils.getBaglist().get(1);
        BagInfoDto bagDto2 = ModelUtils.getBagInfoDto();
        bagDto2.setId(bag2.getId());

        when(modelMapper.map(bag1, BagInfoDto.class)).thenReturn(bagDto1);
        when(modelMapper.map(bag2, BagInfoDto.class)).thenReturn(bagDto2);
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag1));

        long total = bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, bags, tariffsInfo.getId());

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
        OrderInfoDto orderInfoDto = OrderInfoDto.builder()
            .id(order.getId())
            .orderStatus(order.getOrderStatus())
            .orderPrice(1000)
            .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
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

        long sum = bagCalculatorService.getBagsSumToPayInCoins(orderInfoDto);
        long expectedSum = orderBag1.getPrice() * 1 + orderBag2.getPrice() * 2;

        assertEquals(expectedSum, sum);
    }

    @Test
    void bagForUserDtosBuilder_shouldBuildDtos() {
        OrderBag orderBag = ModelUtils.getOrderBag();
        Order order = orderBag.getOrder();
        List<OrderBag> orderBags = List.of(orderBag);
        order.setOrderBags(orderBags);
        OrderInfoDto orderInfoDto = OrderInfoDto.builder()
            .id(order.getId())
            .orderStatus(order.getOrderStatus())
            .orderPrice(1000)
            .build();
        BagForUserDto bagDto = ModelUtils.getBagForUserDto();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderBagService.getActualBagsAmountForOrder(orderBags))
            .thenReturn(Map.of(orderBag.getBag().getId(), 2));
        when(modelMapper.map(orderBags.getFirst(), BagForUserDto.class))
            .thenReturn(bagDto);
        when(moneyConverterUtil.convertCoinsIntoBills(anyLong())).thenReturn(2.0);

        List<BagForUserDto> dtos = bagCalculatorService.bagForUserDtosBuilder(orderInfoDto);

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

        Long sum = bagCalculatorService.calculateBagsSum(dtos);

        assertEquals(1500L, sum);
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenBagNotFound() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        List<BagDto> bags = List.of(ModelUtils.getBagDto());
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt()))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, bags, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenSumLowerThanMin() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(1);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(1000L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        List<BagDto> dtoList = List.of(dto);

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(ModelUtils.getBagInfoDto());
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenSumGreaterThanMax() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(100);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMax(1L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        List<BagDto> dtoList = List.of(dto);

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(ModelUtils.getBagInfoDto());
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenBagsLessThanMin() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(1);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMin(10L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        List<BagDto> dtoList = List.of(dto);

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(ModelUtils.getBagInfoDto());
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenBagsGreaterThanMax() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(100);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMax(1L);

        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(true);
        List<BagDto> dtoList = List.of(dto);

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(ModelUtils.getBagInfoDto());
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldAddNotOrderedBags() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Bag bag = ModelUtils.getBaglist().get(0);
        BagInfoDto bagDto = ModelUtils.getBagInfoDto();
        Bag activeBag = ModelUtils.getBaglist().get(0);
        activeBag.setId(999);
        activeBag.setStatus(BagStatus.ACTIVE);
        BagInfoDto activeBagDto = ModelUtils.getBagInfoDto();
        activeBagDto.setId(activeBag.getId());
        tariffsInfo.setBags(List.of(activeBag));

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(bagDto);
        when(modelMapper.map(activeBag, BagInfoDto.class)).thenReturn(activeBagDto);
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt()))
            .thenReturn(Optional.of(ModelUtils.getBaglist().get(0)));

        long total = bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, List.of(dto), tariffsInfo.getId());

        assertFalse(orderBagList.isEmpty());
        assertTrue(orderBagList.stream().anyMatch(b -> b.getId().equals(999) && b.getAmount() == 0));
        assertTrue(total > 0);
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldThrowWhenAmountLimitNotMet() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(2);

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Bag bag = ModelUtils.getBaglist().get(0);
        bag.setLimitIncluded(false);
        List<BagDto> dtoList = List.of(dto);

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(ModelUtils.getBagInfoDto());
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldPassWhenMinMaxNull() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(null);
        tariffsInfo.setMax(null);

        Bag bag1 = ModelUtils.getBaglist().get(0);
        bag1.setLimitIncluded(true);
        BagInfoDto bagDto1 = ModelUtils.getBagInfoDto();
        List<BagDto> dtoList = List.of(dto);
        Bag bag2 = ModelUtils.getBaglist().get(1);
        BagInfoDto bagDto2 = ModelUtils.getBagInfoDto();
        bagDto2.setId(bag2.getId());

        when(modelMapper.map(bag1, BagInfoDto.class)).thenReturn(bagDto1);
        when(modelMapper.map(bag2, BagInfoDto.class)).thenReturn(bagDto2);
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag1));

        assertDoesNotThrow(
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldPassWhenBagsWithinMinMax() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(2);

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMin(1L);
        tariffsInfo.setMax(3L);

        Bag bag1 = ModelUtils.getBaglist().get(0);
        bag1.setLimitIncluded(true);
        BagInfoDto bagDto1 = ModelUtils.getBagInfoDto();
        List<BagDto> dtoList = List.of(dto);
        Bag bag2 = ModelUtils.getBaglist().get(1);
        BagInfoDto bagDto2 = ModelUtils.getBagInfoDto();
        bagDto2.setId(bag2.getId());

        when(modelMapper.map(bag1, BagInfoDto.class)).thenReturn(bagDto1);
        when(modelMapper.map(bag2, BagInfoDto.class)).thenReturn(bagDto2);
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag1));

        assertDoesNotThrow(
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldIgnoreCheckWhenMinNull() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();
        dto.setAmount(0);

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setMin(null);
        tariffsInfo.setMax(5L);

        Bag bag1 = ModelUtils.getBaglist().get(0);
        bag1.setLimitIncluded(true);
        Bag bag2 = ModelUtils.getBaglist().get(1);
        BagInfoDto bagDto1 = ModelUtils.getBagInfoDto();
        BagInfoDto bagDto2 = ModelUtils.getBagInfoDto();
        bagDto2.setId(bag2.getId());

        when(modelMapper.map(bag1, BagInfoDto.class)).thenReturn(bagDto1);
        when(modelMapper.map(bag2, BagInfoDto.class)).thenReturn(bagDto2);
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(bag1));
        List<BagDto> dtoList = List.of(dto);

        assertDoesNotThrow(
            () -> bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, dtoList, tariffsInfo.getId()));
    }

    @Test
    void prepareBagsAndCalculateTotal_shouldNotAddInactiveBags() {
        List<BagInfoDto> orderBagList = new ArrayList<>();
        BagDto dto = ModelUtils.getBagDto();

        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Bag bag = ModelUtils.getBaglist().get(0);
        BagInfoDto bagDto = ModelUtils.getBagInfoDto();
        Bag inactiveBag = ModelUtils.getBaglist().get(0);
        inactiveBag.setId(123);
        inactiveBag.setStatus(BagStatus.DELETED);
        tariffsInfo.setBags(List.of(inactiveBag));
        BagInfoDto inactiveBagDto = ModelUtils.getBagInfoDto();
        inactiveBagDto.setId(inactiveBag.getId());

        when(modelMapper.map(bag, BagInfoDto.class)).thenReturn(bagDto);
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt()))
            .thenReturn(Optional.of(ModelUtils.getBaglist().get(0)));

        long total = bagCalculatorService.prepareBagsAndCalculateTotal(orderBagList, List.of(dto), tariffsInfo.getId());

        assertTrue(orderBagList.stream().noneMatch(b -> b.getId().equals(123)));
        assertTrue(total > 0);
    }
}