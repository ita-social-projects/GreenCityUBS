package greencity.service.ubs.payment;

import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrderAddress;
import static greencity.ModelUtils.getOrderResponseDto;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getPaymentSystemResponse;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.client.WayForPayClient;
import greencity.constant.OrderHistory;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentSystem;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.order.OrderService;
import greencity.service.ubs.wayforpay.WayForPayService;
import greencity.service.ubs.wayforpay.WayForPayStrategy;
import greencity.util.OrderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UBSUserRepository ubsUserRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentCalculatorService paymentCalculatorService;
    @Mock
    private EventService eventService;
    @Mock
    private WayForPayService wayForPayService;
    @Mock
    private AddressService addressService;
    @Mock
    private OrderService orderService;
    @Mock
    private PaymentStrategyFactory paymentStrategyFactory;
    @Mock
    private WayForPayClient wayForPayClient;
    @Mock
    private ModelMapper modelMapper;

    @Mock private WayForPayStrategy wayForPayStrategy;

    @InjectMocks
    private ProcessPaymentServiceImpl service;

    private final String userUuid = "user-123";
    private final Long orderId = 1L;

    @Test
    void processNewOrder_shouldReturnStrategyResponse_whenShouldBePaidTrue() {
        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        User user = getUser();
        user.setOrders(new ArrayList<>(List.of(order)));
        UBSuser ubsUser = getUBSuser();
        Payment payment = getPayment();
        PaymentSystemResponse paymentSystemResponse = getPaymentSystemResponse();

        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(addressService.checkIfAddressMatchLocationArea(dto.getAddressId(), dto.getLocationId())).thenReturn(true);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(ubsUserRepository.save(any(UBSuser.class))).thenReturn(ubsUser);
        when(orderService.formAndSaveOrderRequest(eq(dto), any(Order.class), eq(user), any(UBSuser.class))).thenReturn(order);
        try (MockedStatic<OrderUtils> mocked = Mockito.mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.getLastPayment(any())).thenReturn(payment);
        }
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(eventService).save(OrderHistory.ORDER_FORMED_UK, OrderHistory.CLIENT_UK, order);
        when(paymentStrategyFactory.getPaymentStrategy(PaymentSystem.WAY_FOR_PAY)).thenReturn(wayForPayStrategy);

        when(wayForPayStrategy.processPayment(any(Order.class), anyLong())).thenReturn(paymentSystemResponse);
        doNothing().when(notificationService).notifyCreatedOrder(order);

        PaymentSystemResponse result = service.processNewOrder(dto, "uuid");

        assertThat(result).isEqualTo(paymentSystemResponse);
        verify(notificationService).notifyCreatedOrder(order);
    }

    @Test
    void processNewOrder_shouldReturnWayForPayResponse_whenShouldBePaidFalse() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.setShouldBePaid(false);
        dto.setLocationId(1L);
        dto.setAddressId(2L);

        Order order = getOrder();
        User user = getUser();
        user.setOrders(new ArrayList<>(List.of(order)));
        UBSuser ubsUser = getUBSuser();

        PaymentSystemResponse paymentSystemResponse = getPaymentSystemResponse();

        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(addressService.checkIfAddressMatchLocationArea(anyLong(), anyLong())).thenReturn(true);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(ubsUserRepository.save(any(UBSuser.class))).thenReturn(ubsUser);
        when(orderService.formAndSaveOrderRequest(eq(dto), any(Order.class), eq(user), any(UBSuser.class)))
            .thenReturn(order);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(wayForPayService.getPaymentRequestDto(order, "")).thenReturn(paymentSystemResponse);
        doNothing().when(notificationService).notifyCreatedOrder(order);
        doNothing().when(eventService).save(anyString(), anyString(), eq(order));

        PaymentSystemResponse result = service.processNewOrder(dto, "uuid");

        assertThat(result).isEqualTo(paymentSystemResponse);
        verify(notificationService).notifyCreatedOrder(order);
    }

    @Test
    void processNewOrder_shouldThrowException_whenAddressNotValid() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.setLocationId(1L);
        dto.setAddressId(2L);

        when(addressService.checkIfAddressMatchLocationArea(anyLong(), anyLong())).thenReturn(false);

        assertThrows(AddressNotWithinLocationAreaException.class,
            () -> service.processNewOrder(dto, "uuid"));
    }

    @Test
    void processExistingOrder_shouldReturnStrategyResponse_whenShouldBePaidTrue() {
        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        User user = getUser();
        user.setOrders(new ArrayList<>(List.of(order)));
        UBSuser ubsUser = getUBSuser();
        Payment payment = getPayment();
        OrderAddress orderAddress = getOrderAddress();
        PaymentSystemResponse paymentSystemResponse = getPaymentSystemResponse();

        when(addressService.checkIfAddressMatchLocationArea(dto.getAddressId(), dto.getLocationId()))
            .thenReturn(true);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(addressService.getOrUpdateOrderAddress(order.getUbsUser().getOrderAddress(), dto.getAddressId(),
            dto.getLocationId(), user))
            .thenReturn(orderAddress);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(ubsUserRepository.save(any(UBSuser.class))).thenReturn(ubsUser);
        when(orderService.formAndSaveOrderRequest(eq(dto), any(Order.class), eq(user), any(UBSuser.class)))
            .thenReturn(order);
        try (MockedStatic<OrderUtils> mocked = Mockito.mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.getLastPayment(any())).thenReturn(payment);
        }
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(eventService).save(OrderHistory.ORDER_STATUS_UPDATED_UK, OrderHistory.CLIENT_UK, order);
        when(paymentStrategyFactory.getPaymentStrategy(PaymentSystem.WAY_FOR_PAY)).thenReturn(wayForPayStrategy);

        when(wayForPayStrategy.processPayment(any(Order.class), anyLong())).thenReturn(paymentSystemResponse);
        doNothing().when(notificationService).notifyUnpaidOrderPermanently(any(Order.class), anyLong(), any(PaymentSystemResponse.class));

        PaymentSystemResponse result = service.processExistingOrder(dto, "uuid", order.getId());

        assertThat(result).isEqualTo(paymentSystemResponse);
        verify(notificationService).notifyUnpaidOrderPermanently(any(Order.class), anyLong(), any(PaymentSystemResponse.class));
    }

    @Test
    void processExistingOrder_shouldThrowException_whenWrongUserId() {
        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        order.getUser().setId(2L);
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        User user = getUser();
        user.setOrders(new ArrayList<>(List.of(order)));

        when(addressService.checkIfAddressMatchLocationArea(dto.getAddressId(), dto.getLocationId()))
            .thenReturn(true);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findByUuid("uuid")).thenReturn(user);

        assertThrows(AccessDeniedException.class,
            () -> service.processExistingOrder(dto, "uuid", order.getId()));
    }

    @Test
    void processExistingOrder_shouldThrowException_whenWrongOrderStatus() {
        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        User user = getUser();
        user.setOrders(new ArrayList<>(List.of(order)));

        when(addressService.checkIfAddressMatchLocationArea(dto.getAddressId(), dto.getLocationId()))
            .thenReturn(true);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findByUuid("uuid")).thenReturn(user);

        assertThrows(BadRequestException.class,
            () -> service.processExistingOrder(dto, "uuid", order.getId()));
    }

    @Test
    void processExistingOrder_shouldThrowException_whenWrongOrderPaymentStatus() {
        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        User user = getUser();
        user.setOrders(new ArrayList<>(List.of(order)));

        when(addressService.checkIfAddressMatchLocationArea(dto.getAddressId(), dto.getLocationId()))
            .thenReturn(true);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findByUuid("uuid")).thenReturn(user);

        assertThrows(BadRequestException.class,
            () -> service.processExistingOrder(dto, "uuid", order.getId()));
    }

    @Test
    void shouldThrowNotFoundException_whenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        assertThrows(NotFoundException.class, () -> service.processOrder(userUuid, dto));
    }

    @Test
    void shouldThrowNotFoundException_whenUserNotFound() {
        Order order = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(userUuid)).thenReturn(Optional.empty());

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        assertThrows(NotFoundException.class, () -> service.processOrder(userUuid, dto));
    }

    @Test
    void shouldThrowBadRequestException_whenOrderAlreadyPaid() {
        Order order = new Order();
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        assertThrows(BadRequestException.class, () -> service.processOrder(userUuid, dto));
    }

    @Test
    void shouldIncrementCounter_whenCounterIsNull() {
        Order order = getOrder();
        order.setCounterOrderPaymentId(null);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        PaymentSystemResponse paymentSystemResponse = getPaymentSystemResponse();
        PaymentWayForPayRequestDto payRequestDto = new PaymentWayForPayRequestDto();

        User user = new User();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(userUuid)).thenReturn(Optional.of(user));
        when(paymentCalculatorService.calculateSumToPay(any(), any(), any())).thenReturn(100L);
        when(wayForPayService.getPaymentRequestDto(any(), any())).thenReturn(paymentSystemResponse);
        when(wayForPayService.formPaymentRequestForWayForPay(anyLong(), anyLong()))
            .thenReturn(payRequestDto);
        when(wayForPayService.getLinkFromWayForPayCheckoutResponse(anyString())).thenReturn("Response");
        when(wayForPayClient.getCheckOutResponse(payRequestDto)).thenReturn("link");

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        service.processOrder(userUuid, dto);

        assertEquals(1L, order.getCounterOrderPaymentId());
    }

    @Test
    void shouldReturnDtoWithNullLink_whenSumToPayIsZeroOrNegative() {
        Order order = new Order();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);

        User user = new User();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(userUuid)).thenReturn(Optional.of(user));
        when(paymentCalculatorService.calculateSumToPay(any(), any(), any())).thenReturn(0L);
        when(wayForPayService.getPaymentRequestDto(order, null)).thenReturn(
            PaymentSystemResponse.builder().orderId(orderId).link(null).build());

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        PaymentSystemResponse response = service.processOrder(userUuid, dto);

        assertNull(response.link());
        verify(eventService).save(anyString(), anyString(), eq(order));
    }

    @Test
    void shouldReturnDtoWithLink_whenSumToPayIsPositive() {
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        PaymentWayForPayRequestDto payRequestDto = new PaymentWayForPayRequestDto();

        User user = new User();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(userUuid)).thenReturn(Optional.of(user));
        when(paymentCalculatorService.calculateSumToPay(any(), any(), any())).thenReturn(500L);
        when(wayForPayService.getPaymentRequestDto(eq(order), anyString()))
            .thenReturn(PaymentSystemResponse.builder().orderId(orderId).link("link").build());
        when(wayForPayService.formPaymentRequestForWayForPay(anyLong(), anyLong()))
            .thenReturn(payRequestDto);
        when(wayForPayService.getLinkFromWayForPayCheckoutResponse(anyString())).thenReturn("Response");
        when(wayForPayClient.getCheckOutResponse(payRequestDto)).thenReturn("link");

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        PaymentSystemResponse response = service.processOrder(userUuid, dto);

        assertEquals("link", response.link());
        verify(eventService, never()).save(anyString(), anyString(), eq(order));
    }
}