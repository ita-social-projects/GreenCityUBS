package greencity.service.ubs.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.CancellationReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.calculator.BagCalculatorService;
import greencity.service.ubs.calculator.CertificateCalculatorService;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.calculator.PointCalculatorService;
import greencity.util.MoneyConverterUtil;
import greencity.util.PointsUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private BagCalculatorService bagCalculatorService;
    @Mock
    private PointCalculatorService pointCalculatorService;
    @Mock
    private CertificateCalculatorService certificateCalculatorService;
    @Mock
    private PaymentCalculatorService paymentCalculatorService;
    @Mock
    private OrdersForUserRepository ordersForUserRepository;
    @Mock
    private PointsUtils pointsUtils;
    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;
    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    @Mock
    private MoneyConverterUtil moneyConverterUtil;
    @Mock
    private UserRepository userRepository;

    private User user;
    private UBSuser ubsUser;
    private Order order;
    private TariffsInfo tariffsInfo;
    private OrderAddress orderAddress;

    //TODO fix, refactor and cover all code

    @BeforeEach
    void setUp() {
        orderAddress = ModelUtils.getOrderAddress();

        user = new User();
        user.setId(1L);
        user.setCurrentPoints(100);
        user.setChangeOfPointsList(new ArrayList<>());

        ubsUser = new UBSuser();
        ubsUser.setOrderAddress(orderAddress);
        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setUbsUser(ubsUser);
        order.setPointsToUse(0);
        order.setCertificates(new HashSet<>());
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setSumTotalAmountWithoutDiscounts(1000L); // щоб не було NPE
        order.setPayment(new ArrayList<>());
        order.setCounterOrderPaymentId(0L);

        tariffsInfo = new TariffsInfo();
        tariffsInfo.setId(1L);
    }

    @Test
    void formAndSaveOrderRequest_success() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setBags(Collections.emptyList());
        dto.setPointsToUse(10);
        dto.setLocationId(1L);

        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagCalculatorService.prepareBagsAndCalculateTotal(anyList(), anyList(), any()))
            .thenReturn(100L);
        when(pointCalculatorService.reduceOrderSumDueToUsedPoints(anyLong(), anyInt())).thenReturn(90L);
        when(certificateCalculatorService.applyCertificatesToOrder(any(), any(), any(), anyLong())).thenReturn(90L);
        when(orderRepository.save(any())).thenReturn(order);
        doNothing().when(pointsUtils).checkIfUserHaveEnoughPoints(anyInt(), anyInt());
        when(paymentCalculatorService.calculateOrderSumWithoutDiscounts(anyList())).thenReturn(100L);

        Order result = orderService.formAndSaveOrderRequest(dto, order, user, ubsUser);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getTariffsInfo()).isEqualTo(tariffsInfo);
    }

    @Test
    void transferUserPointsToOrder_success() {
        order.setPointsToUse(0);
        user.setCurrentPoints(100); // достатньо балів
        user.setChangeOfPointsList(new ArrayList<>()); // ⚡ додано

        doNothing().when(pointsUtils).checkIfUserHaveEnoughPoints(anyInt(), anyInt());
        when(orderRepository.save(any())).thenReturn(order);

        orderService.transferUserPointsToOrder(order, 10); // ⚡ використовуємо 50 балів

        assertThat(order.getPointsToUse()).isEqualTo(10);
        assertThat(user.getCurrentPoints()).isEqualTo(90);
        assertThat(user.getChangeOfPointsList()).hasSize(1);
    }


    @Test
    void transferUserPointsToOrder_tooManyPoints() {
        order.setPointsToUse(0);
        user.setCurrentPoints(100);

        doNothing().when(pointsUtils).checkIfUserHaveEnoughPoints(anyInt(), anyInt());
        // simulate maxPointsToTransfer < pointsToUse
        order.setSumTotalAmountWithoutDiscounts(2000L); // coins
        assertThatThrownBy(() -> orderService.transferUserPointsToOrder(order, 1000))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getOrderForUser_notFound() {
        when(ordersForUserRepository.getAllByUserUuidAndId("uuid", 1L)).thenReturn(null);

        assertThatThrownBy(() -> orderService.getOrderForUser("uuid", 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteOrder_success() {
        when(ordersForUserRepository.getAllByUserUuidAndId("uuid", 1L)).thenReturn(order);

        orderService.deleteOrder("uuid", 1L);

        verify(orderRepository).saveAndFlush(order);
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_notFound() {
        when(ordersForUserRepository.getAllByUserUuidAndId("uuid", 1L)).thenReturn(null);

        assertThatThrownBy(() -> orderService.deleteOrder("uuid", 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOrdersData_buildsDto() {
        OrderStatusTranslation statusTranslation = new OrderStatusTranslation();
        statusTranslation.setNameUk("ukr");
        statusTranslation.setNameEn("en");

        OrderPaymentStatusTranslation paymentStatusTranslation = new OrderPaymentStatusTranslation();
        paymentStatusTranslation.setTranslationValueUk("ukr");
        paymentStatusTranslation.setTranslationsValueEn("en");

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(anyLong()))
            .thenReturn(Optional.of(statusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(anyLong()))
            .thenReturn(paymentStatusTranslation);
        when(bagCalculatorService.bagForUserDtosBuilder(any())).thenReturn(Collections.emptyList());
        when(paymentCalculatorService.countPaidAmount(any())).thenReturn(0L);
        when(certificateCalculatorService.countCertificatesBonuses(any())).thenReturn(0);
        when(moneyConverterUtil.convertCoinsIntoBills(anyLong())).thenReturn(1.0);

        OrdersDataForUserDto resultOrder = orderService.getOrdersData(order);

        assertThat(resultOrder).isNotNull();
        assertThat(resultOrder.getOrderStatusUk()).isEqualTo("ukr");
    }

    @Test
    void getOrdersForUser_returnsPageableDto() {
        OrderStatusTranslation statusTranslation = new OrderStatusTranslation();
        statusTranslation.setNameUk("ukr");
        statusTranslation.setNameEn("en");

        OrderPaymentStatusTranslation paymentStatusTranslation = new OrderPaymentStatusTranslation();
        paymentStatusTranslation.setTranslationValueUk("ukr");
        paymentStatusTranslation.setTranslationsValueEn("en");

        Page<Order> orderPage = new PageImpl<>(
            Collections.singletonList(order),
            PageRequest.of(0, 10),
            1
        );
        when(ordersForUserRepository.getAllByUserUuid(any(Pageable.class), anyString()))
            .thenReturn(orderPage);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(anyLong()))
            .thenReturn(Optional.of(statusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(anyLong()))
            .thenReturn(paymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> result = orderService.getOrdersForUser("uuid", PageRequest.of(0, 10), null);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).hasSize(1);
        assertThat(result.getPage().get(0).getOrderStatusUk()).isEqualTo("ukr");
    }

    @Test
    void getOrdersForUser_withStatuses_returnsPageableDto() {
        OrderStatusTranslation statusTranslation = new OrderStatusTranslation();
        statusTranslation.setNameUk("ukr");
        statusTranslation.setNameEn("en");

        OrderPaymentStatusTranslation paymentStatusTranslation = new OrderPaymentStatusTranslation();
        paymentStatusTranslation.setTranslationValueUk("ukr");
        paymentStatusTranslation.setTranslationsValueEn("en");

        Page<Order> orderPage = new PageImpl<>(
            Collections.singletonList(order),
            PageRequest.of(0, 10),
            1
        );
        when(ordersForUserRepository.getAllByUserUuidAndOrderStatusIn(any(Pageable.class), anyString(), anyList()))
            .thenReturn(orderPage);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(anyLong()))
            .thenReturn(Optional.of(statusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(anyLong()))
            .thenReturn(paymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> result = orderService.getOrdersForUser("uuid", PageRequest.of(0, 10),
            Collections.singletonList(OrderStatus.FORMED));

        assertThat(result).isNotNull();
        assertThat(result.getPage()).hasSize(1);
    }

    @Test
    void getOrderPaymentDetail_returnsDto() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderPaymentDetailDto dto = orderService.getOrderPaymentDetail(1L);

        assertThat(dto).isNotNull();
    }

    @Test
    void getOrderCancellationReason_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUuid("uuid")).thenReturn(user);

        // Для тесту користувач повинен бути власником замовлення
        order.setUser(user);
        order.setCancellationReason(CancellationReason.OTHER);
        order.setCancellationComment("comment");

        OrderCancellationReasonDto dto = orderService.getOrderCancellationReason(1L, "uuid");

        assertThat(dto.getCancellationReason()).isEqualTo(CancellationReason.OTHER);
        assertThat(dto.getCancellationComment()).isEqualTo("comment");
    }

    @Test
    void getOrderCancellationReason_accessDenied() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        User anotherUser = new User();
        when(userRepository.findByUuid("uuid")).thenReturn(anotherUser);

        assertThatThrownBy(() -> orderService.getOrderCancellationReason(1L, "uuid"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void formAndSaveOrderRequest_pointsNotUsed_setsPointsToZero() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setBags(Collections.emptyList());
        dto.setPointsToUse(0); // points не використані
        dto.setLocationId(1L);

        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagCalculatorService.prepareBagsAndCalculateTotal(anyList(), anyList(), any()))
            .thenReturn(100L);
        when(pointCalculatorService.reduceOrderSumDueToUsedPoints(anyLong(), anyInt()))
            .thenReturn(100L); // sumToPayInCoins == sumToPayInCoinsWithoutDiscount
        when(orderRepository.save(any())).thenReturn(order);

        orderService.formAndSaveOrderRequest(dto, order, user, ubsUser);

        assertThat(order.getPointsToUse()).isZero();
        assertThat(dto.getPointsToUse()).isZero();
    }

    @Test
    void transferUserPointsToOrder_pointsZero_doesNothing() {
        order.setPointsToUse(0);
        orderService.transferUserPointsToOrder(order, 0);
        assertThat(order.getPointsToUse()).isZero();
        assertThat(user.getCurrentPoints()).isEqualTo(100);
    }
}