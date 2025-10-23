package greencity.service.ubs.payment;

import static greencity.ModelUtils.getCertificate;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrderAddress;
import static greencity.ModelUtils.getOrderAddressDto;
import static greencity.ModelUtils.getOrderCount;
import static greencity.ModelUtils.getOrderInfoDto;
import static greencity.ModelUtils.getOrderResponseDto;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getPaymentSystemResponse;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserWithInitializedFields;
import static greencity.constant.ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.UNABLE_TO_CANCEL_PAYMENT_INVOICE;
import static greencity.constant.QuartzConstants.NO_PAYMENT_ATTEMPT_FOR_ORDER;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_CANCEL_EXCEPTION;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static greencity.constant.QuartzConstants.QUARTZ_SCHEDULER_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.client.WayForPayClient;
import greencity.constant.OrderHistory;
import greencity.dto.order.OrderAddressDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.CertificateStatus;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentSystem;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.CertificateRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.OrderBagService;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.order.OrderService;
import greencity.service.ubs.wayforpay.WayForPayService;
import greencity.service.ubs.wayforpay.WayForPayStrategy;
import greencity.util.OrderUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceImplTest {
    private final String userUuid = "user-123";
    private final Long orderId = 1L;
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
    @Mock
    private JobDetail jobDetail;
    @Mock
    private Scheduler quartzScheduler;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private WayForPayStrategy wayForPayStrategy;
    @Mock
    private OrderAddressRepository orderAddressRepository;
    @Mock
    private OrderBagService orderBagService;
    @InjectMocks
    private ProcessPaymentServiceImpl service;

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
        when(orderService.formAndSaveOrderRequest(eq(dto), any(Long.class), eq(user.getId()), nullable(Long.class)))
            .thenReturn(order.getId());
        when(addressService.formAndSaveOrderAddress(dto.getAddressId(), dto.getLocationId(), user.getId()))
            .thenReturn(getOrderAddressDto());
        try (MockedStatic<OrderUtils> mocked = Mockito.mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.getLastPayment(any())).thenReturn(payment);
            when(userRepository.save(any(User.class))).thenReturn(user);
            doNothing().when(eventService).save(OrderHistory.ORDER_FORMED_UK, OrderHistory.CLIENT_UK, order.getId());
            when(paymentStrategyFactory.getPaymentStrategy(PaymentSystem.WAY_FOR_PAY)).thenReturn(wayForPayStrategy);
            when(wayForPayStrategy.processPayment(any(OrderResponseDto.class), any(Long.class), anyLong()))
                .thenReturn(paymentSystemResponse);
            when(orderAddressRepository.findById(dto.getAddressId()))
                .thenReturn(Optional.of(order.getUbsUser().getOrderAddress()));
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            doNothing().when(notificationService).notifyCreatedOrder(order.getId());

            PaymentSystemResponse result = service.processNewOrder(dto, "uuid");

            assertThat(result).isEqualTo(paymentSystemResponse);
            verify(notificationService).notifyCreatedOrder(order.getId());
        }
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
        when(addressService.formAndSaveOrderAddress(dto.getAddressId(), dto.getLocationId(), user.getId()))
            .thenReturn(getOrderAddressDto());
        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(addressService.checkIfAddressMatchLocationArea(anyLong(), anyLong())).thenReturn(true);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(ubsUserRepository.save(any(UBSuser.class))).thenReturn(ubsUser);
        when(orderService.formAndSaveOrderRequest(eq(dto), any(Long.class), eq(user.getId()), nullable(Long.class)))
            .thenReturn(order.getId());
        when(orderAddressRepository.findById(ubsUser.getOrderAddress().getId()))
            .thenReturn(Optional.of(getOrderAddress()));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(wayForPayService.getPaymentRequestDto(order.getId(), "")).thenReturn(paymentSystemResponse);
        doNothing().when(notificationService).notifyCreatedOrder(order.getId());
        doNothing().when(eventService).save(anyString(), anyString(), eq(order.getId()));

        PaymentSystemResponse result = service.processNewOrder(dto, "uuid");

        assertThat(result).isEqualTo(paymentSystemResponse);
        verify(notificationService).notifyCreatedOrder(order.getId());
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
        OrderAddressDto orderAddressDto = getOrderAddressDto();
        PaymentSystemResponse paymentSystemResponse = getPaymentSystemResponse();

        when(addressService.checkIfAddressMatchLocationArea(dto.getAddressId(), dto.getLocationId()))
            .thenReturn(true);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(addressService.getOrUpdateOrderAddress(orderAddressDto, dto.getAddressId(), dto.getLocationId(),
            user.getId()))
            .thenReturn(orderAddressDto);
        when(modelMapper.map(order.getUbsUser().getOrderAddress(), OrderAddressDto.class)).thenReturn(orderAddressDto);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(ubsUserRepository.save(any(UBSuser.class))).thenReturn(ubsUser);
        when(orderService.formAndSaveOrderRequest(eq(dto), any(Long.class), eq(user.getId()), any(Long.class)))
            .thenReturn(order.getId());
        try (MockedStatic<OrderUtils> mocked = Mockito.mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.getLastPayment(any())).thenReturn(payment);
            when(userRepository.save(any(User.class))).thenReturn(user);
            doNothing().when(eventService).save(OrderHistory.ORDER_STATUS_UPDATED_UK, OrderHistory.CLIENT_UK,
                order.getId());
            when(paymentStrategyFactory.getPaymentStrategy(PaymentSystem.WAY_FOR_PAY)).thenReturn(wayForPayStrategy);
            when(orderAddressRepository.findById(ubsUser.getOrderAddress().getId()))
                .thenReturn(Optional.of(getOrderAddress()));
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(wayForPayStrategy.processPayment(any(OrderResponseDto.class), any(Long.class), anyLong()))
                .thenReturn(paymentSystemResponse);
            doNothing().when(notificationService).notifyUnpaidOrderPermanently(any(Long.class), anyLong(),
                any(PaymentSystemResponse.class));

            PaymentSystemResponse result = service.processExistingOrder(dto, "uuid", order.getId());

            assertThat(result).isEqualTo(paymentSystemResponse);
            verify(notificationService).notifyUnpaidOrderPermanently(any(Long.class), anyLong(),
                any(PaymentSystemResponse.class));
        }
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
            () -> service.processExistingOrder(dto, "uuid", orderId));
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
            () -> service.processExistingOrder(dto, "uuid", orderId));
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
            () -> service.processExistingOrder(dto, "uuid", orderId));
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
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));
        when(modelMapper.map(order, OrderInfoDto.class)).thenReturn(getOrderInfoDto());
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
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);

        User user = new User();
        when(modelMapper.map(order, OrderInfoDto.class)).thenReturn(getOrderInfoDto());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(userUuid)).thenReturn(Optional.of(user));
        when(paymentCalculatorService.calculateSumToPay(any(), any(), any())).thenReturn(0L);
        when(wayForPayService.getPaymentRequestDto(order.getId(), null)).thenReturn(
            PaymentSystemResponse.builder().orderId(order.getId()).link(null).build());

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        PaymentSystemResponse response = service.processOrder(userUuid, dto);

        assertNull(response.link());
        verify(eventService).save(anyString(), anyString(), eq(order.getId()));
    }

    @Test
    void shouldReturnDtoWithLink_whenSumToPayIsPositive() {
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        PaymentWayForPayRequestDto payRequestDto = new PaymentWayForPayRequestDto();

        User user = new User();
        when(modelMapper.map(order, OrderInfoDto.class)).thenReturn(getOrderInfoDto());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(userUuid)).thenReturn(Optional.of(user));
        when(paymentCalculatorService.calculateSumToPay(any(), any(), any())).thenReturn(500L);
        when(wayForPayService.getPaymentRequestDto(eq(order.getId()), anyString()))
            .thenReturn(PaymentSystemResponse.builder().orderId(orderId).link("link").build());
        when(wayForPayService.formPaymentRequestForWayForPay(anyLong(), anyLong()))
            .thenReturn(payRequestDto);
        when(wayForPayService.getLinkFromWayForPayCheckoutResponse(anyString())).thenReturn("Response");
        when(wayForPayClient.getCheckOutResponse(payRequestDto)).thenReturn("link");

        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setOrderId(orderId);

        PaymentSystemResponse response = service.processOrder(userUuid, dto);

        assertEquals("link", response.link());
        verify(eventService, never()).save(anyString(), anyString(), eq(order.getId()));
    }

    @Test
    void cancelPaymentAttempt() throws SchedulerException, JSONException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        String response = new JSONObject().put("reason", "Removed").toString();

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(wayForPayService.formPaymentCancellationRequestForWayForPay(order.getId()))
            .thenReturn(new PaymentCancellationWayForPayRequestDto());
        when(wayForPayClient.getCancellationResponse(any(PaymentCancellationWayForPayRequestDto.class)))
            .thenReturn(response);
        when(wayForPayService.getResultFromWayForPayCancellationResponse(anyString())).thenReturn("Removed");

        service.cancelPaymentAttempt(uuid, orderId);
    }

    @Test
    void cancelPaymentAttemptWhenNoPending() {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertTrue(exception.getMessage().contains(NO_PAYMENT_ATTEMPT_FOR_ORDER));
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerFailsToCheck() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        doThrow(SchedulerException.class).when(quartzScheduler).checkExists(any(JobKey.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenOrderNotFound() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertTrue(exception.getMessage().contains(ORDER_NOT_FOUND_BY_ID));
    }

    @Test
    void cancelPaymentAttemptWhenOrderNotBelongsToUser() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        order.setUser(getUserWithInitializedFields().setId(2L));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        when(quartzScheduler
            .checkExists(JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP)))
            .thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertEquals(ORDER_DOES_NOT_BELONG_TO_USER, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerFailsToGetJobData() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doThrow(SchedulerException.class).when(quartzScheduler).getJobDetail(jobKey);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerReturnsFalseOnCancel() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());
        IllegalStateException illegalStateException = new IllegalStateException(PAYMENT_EXPIRY_CANCEL_EXCEPTION);

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        doThrow(illegalStateException).when(orderService).cancelPaymentExpiryJob(orderId);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertEquals(PAYMENT_EXPIRY_CANCEL_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerFailsToCancelJob() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());
        IllegalStateException illegalStateException = new IllegalStateException(QUARTZ_SCHEDULER_EXCEPTION);

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        doThrow(illegalStateException).when(orderService).cancelPaymentExpiryJob(orderId);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenWayForPayDeclines() throws SchedulerException, JSONException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        String response = new JSONObject().put("reason", "Any wrong reason").toString();

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(wayForPayService.formPaymentCancellationRequestForWayForPay(order.getId()))
            .thenReturn(new PaymentCancellationWayForPayRequestDto());
        when(wayForPayClient.getCancellationResponse(any(PaymentCancellationWayForPayRequestDto.class)))
            .thenReturn(response);
        when(wayForPayService.getResultFromWayForPayCancellationResponse(anyString())).thenReturn("Any wrong reason");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> service.cancelPaymentAttempt(uuid, orderId));

        assertEquals(UNABLE_TO_CANCEL_PAYMENT_INVOICE, exception.getMessage());
    }

    @Test
    void expirePaymentAttempt() {
        Order order = getOrder();
        HashSet<Certificate> certificates = new HashSet<>(List.of(
            getCertificate().setCode("7777-7777").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(100).setPoints(100),
            getCertificate().setCode("1111-1111").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(120).setPoints(100)));
        order.setCertificates(certificates);
        HashSet<String> certificateCodes = new HashSet<>(List.of("7777-7777", "1111-1111"));
        List<String> certificateCodesList = certificateCodes.stream().toList();
        int pointsUsed = order.getPointsToUse();

        User user = getUserWithInitializedFields();
        int pointsBefore = user.getCurrentPoints();
        order.setUser(user);

        HashSet<Certificate> expectedCertificates = new HashSet<>();
        Certificate expectedCertificate1 = getCertificate()
            .setCode("7777-7777")
            .setOrder(null)
            .setDateOfUse(null)
            .setCertificateStatus(CertificateStatus.ACTIVE)
            .setInitialPointsValue(100)
            .setPoints(100);
        expectedCertificate1.setPoints(expectedCertificate1.getInitialPointsValue());
        expectedCertificates.add(expectedCertificate1);
        Certificate expectedCertificate2 = getCertificate()
            .setCode("1111-1111")
            .setOrder(null)
            .setDateOfUse(null)
            .setCertificateStatus(CertificateStatus.ACTIVE)
            .setInitialPointsValue(120)
            .setPoints(120);
        expectedCertificate2.setPoints(expectedCertificate2.getInitialPointsValue());
        expectedCertificates.add(expectedCertificate2);

        when(certificateRepository.findAllByCodesAndOrderId(certificateCodesList, orderId))
            .thenReturn(certificates);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.expirePaymentAttempt(orderId, pointsUsed, certificateCodes);

        assertEquals(pointsBefore + pointsUsed, user.getCurrentPoints());
        assertEquals(expectedCertificates, certificates);
        assertEquals("", order.getPaymentLink());
        assertNull(order.getPaymentLinkExpiry());

        verify(certificateRepository).findAllByCodesAndOrderId(certificateCodesList, orderId);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void expirePaymentAttemptWhenNoBonusesSpecified() {
        Order order = getOrder();
        HashSet<Certificate> certificates = new HashSet<>(List.of(
            getCertificate().setCode("7777-7777").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(100).setPoints(100),
            getCertificate().setCode("1111-1111").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(120).setPoints(100)));
        order.setCertificates(certificates);
        int orderPointsBefore = order.getPointsToUse();

        User user = getUserWithInitializedFields();
        int userPointsBefore = user.getCurrentPoints();
        order.setUser(user);

        HashSet<Certificate> expectedCertificates = new HashSet<>(List.of(
            getCertificate().setCode("7777-7777").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(100).setPoints(100),
            getCertificate().setCode("1111-1111").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(120).setPoints(100)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.expirePaymentAttempt(orderId, 0, new HashSet<>());

        assertEquals(orderPointsBefore, order.getPointsToUse());
        assertEquals(userPointsBefore, user.getCurrentPoints());
        assertEquals(expectedCertificates, certificates);
        assertEquals("", order.getPaymentLink());
        assertNull(order.getPaymentLinkExpiry());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);

        verifyNoInteractions(certificateRepository);
    }

    @Test
    void expirePaymentAttemptWhenOrderNotExists() {
        HashSet<String> certificateCodes = new HashSet<>(List.of("7777-7777", "1111-1111"));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> service.expirePaymentAttempt(orderId, 0, certificateCodes));

        assertTrue(exception.getMessage().contains(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
    }

    @Test
    void formedLink() {
        String invoiceUrl = "https://pay.example.com/invoice/TEST123";
        Order order = getOrderCount();
        order.setPayment(List.of(getPayment()));
        order.setPaymentLink(" ");
        PaymentWayForPayRequestDto payRequestDto = new PaymentWayForPayRequestDto();
        OrderWayForPayClientDto dto = ModelUtils.getOrderWayForPayClientDto();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(wayForPayClient.getCheckOutResponse(any()))
            .thenReturn("{\"invoiceUrl\":\"https://pay.example.com/invoice/TEST123\"}");
        when(wayForPayService.formPaymentRequestForWayForPay(anyLong(), anyLong()))
            .thenReturn(payRequestDto);
        when(wayForPayService.getLinkFromWayForPayCheckoutResponse(anyString())).thenReturn(invoiceUrl);

        String result = service.formedLink(order.getId(), 560, dto);

        assertEquals(invoiceUrl, result);
    }
}