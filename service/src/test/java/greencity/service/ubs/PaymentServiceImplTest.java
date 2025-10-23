package greencity.service.ubs;

import static greencity.ModelUtils.TEST_EMAIL;
import static greencity.ModelUtils.getCertificateList;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getFormedOrder;
import static greencity.ModelUtils.getInfoPayment;
import static greencity.ModelUtils.getManualPayment;
import static greencity.ModelUtils.getManualPaymentRequestDto;
import static greencity.ModelUtils.getManualPaymentRequestDtoWithoutImage;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrderForGetOrderStatusData2Test;
import static greencity.ModelUtils.getOrderUserFirst;
import static greencity.ModelUtils.getOrderWithoutPayment;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getRefundDto_NothingToRefund;
import static greencity.ModelUtils.getRefundDto_ReturnBonuses;
import static greencity.ModelUtils.getRefundDto_ReturnMoney;
import static greencity.ModelUtils.getRefundDto_ReturnMoneyAndBonuses;
import static greencity.ModelUtils.getTariffsInfo;
import static greencity.ModelUtils.getTestUser;
import static greencity.constant.ErrorMessage.CANNOT_REFUND_MONEY;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_MONEY_REFUND;
import static greencity.constant.ErrorMessage.INVALID_REQUESTED_REFUND_AMOUNT;
import static greencity.constant.ErrorMessage.ORDER_CAN_NOT_BE_UPDATED;
import static greencity.constant.ErrorMessage.ORDER_HAS_NO_OVERPAYMENT;
import static greencity.constant.ErrorMessage.REFUND_CONFLICT_MONEY_AND_BONUSES;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.client.config.UserRemoteWebClient;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.refund.RefundDto;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.order.Refund;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.enums.BonusReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.CertificateRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.RefundRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.notification.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CertificateRepository certificateRepository;
    @Mock
    WebClientRequestException webClientRequestException;
    @Mock
    TariffsInfoRepository tariffsInfoRepository;
    @Mock
    RefundRepository refundRepository;
    @Mock
    private UserRemoteWebClient userRemoteWebClient;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private NotificationServiceImpl notificationService;
    @Mock
    private EventService eventService;
    @Mock
    private OrderBagService orderBagService;
    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    @Test
    void checkGetPaymentInfo() {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertEquals(100L, paymentServiceImpl.getPaymentInfo(order.getId(), 800.).getOverpayment());
        assertEquals(200L, paymentServiceImpl.getPaymentInfo(order.getId(), 100.).getPaidAmount());
        assertEquals(0L, paymentServiceImpl.getPaymentInfo(order.getId(), 100.).getUnPaidAmount());
        verify(orderRepository, times(3)).findById(order.getId());
    }

    @Test
    void checkGetPaymentInfoIfOrderStatusIsCanceled() {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertEquals(200L, paymentServiceImpl.getPaymentInfo(order.getId(), 800.).getOverpayment());
        assertEquals(200L, paymentServiceImpl.getPaymentInfo(order.getId(), 100.).getPaidAmount());
        assertEquals(0L, paymentServiceImpl.getPaymentInfo(order.getId(), 100.).getUnPaidAmount());
        verify(orderRepository, times(3)).findById(order.getId());
    }

    @Test
    void checkGetPaymentInfoIfSumToPayIsNull() {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        log.info(order.toString());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertEquals(900L, paymentServiceImpl.getPaymentInfo(order.getId(), null).getOverpayment());
        verify(orderRepository).findById(order.getId());
    }

    @Test
    void getPaymentInfo() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        PaymentInfoDto paymentInfo = getInfoPayment();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(modelMapper.map(any(), eq(PaymentInfoDto.class))).thenReturn(paymentInfo);

        assertEquals(ModelUtils.getPaymentTableInfoDto(), paymentServiceImpl.getPaymentInfo(order.getId(), 100.));
    }

    @Test
    void getPaymentInfoWithoutPayment() {
        Order order = getOrderWithoutPayment();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(ModelUtils.getPaymentTableInfoDto2(), paymentServiceImpl.getPaymentInfo(order.getId(), 100.));
    }

    @Test
    void getPaymentInfoExceptionTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> paymentServiceImpl.getPaymentInfo(1L, 100.));
    }

    @Test
    void checkGetPaymentInfoWhenPaymentsWithCertificatesAndPointsSmallerThanSumToPay() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(0L, paymentServiceImpl.getPaymentInfo(order.getId(), 1100.).getOverpayment());
    }

    @Test
    void checkDeleteManualPayment() {
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        doNothing().when(paymentRepository).deletePaymentById(1L);
        doNothing().when(userRemoteWebClient).deleteFile("");
        doNothing().when(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY_UK + getManualPayment().getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder().getId());
        paymentServiceImpl.deleteManualPayment(1L, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).deletePaymentById(1L);
    }

    @Test
    void deleteManualPaymentWebClientRequestException() {
        LogCaptor logCaptor = LogCaptor.forClass(PaymentServiceImpl.class);
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        doNothing().when(paymentRepository).deletePaymentById(1L);
        doThrow(webClientRequestException)
            .when(userRemoteWebClient).deleteFile(anyString());
        doNothing().when(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY_UK + getManualPayment().getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder().getId());
        paymentServiceImpl.deleteManualPayment(1L, "abc");
        List<String> warns = logCaptor.getWarnLogs();
        assertTrue(warns.getFirst().contains("User service is unavailable: null"));
    }

    @Test
    void deleteManualPaymentTest() {
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        Order order = getFormedOrder();

        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentServiceImpl.deleteManualPayment(1L, "abc");

        verify(employeeRepository).findByUuid("abc");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deletePaymentById(1L);
        verify(userRemoteWebClient).deleteFile(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY_UK + payment.getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder().getId());

    }

    @Test
    void deleteManualTestWithoutImage() {
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        payment.setImagePath(null);

        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentServiceImpl.deleteManualPayment(1L, "abc");

        verify(employeeRepository).findByUuid("abc");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deletePaymentById(1L);
        verify(userRemoteWebClient, times(0)).deleteFile(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY_UK + payment.getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder().getId());
    }

    @Test
    void deleteManualTestWithoutUser() {
        when(employeeRepository.findByUuid("uuid25")).thenReturn(Optional.empty());
        EntityNotFoundException ex =
            assertThrows(EntityNotFoundException.class, () -> paymentServiceImpl.deleteManualPayment(1L, "uuid25"));
        assertEquals(EMPLOYEE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void deleteManualTestWithoutPayment() {
        Employee employee = getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(paymentRepository.findById(25L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> paymentServiceImpl.deleteManualPayment(25L, "abc"));
    }

    @Test
    void checkUpdateManualPayment() {
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY_UK + 1,
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder().getId());
        paymentServiceImpl.updateManualPayment(1L, getManualPaymentRequestDto(), null, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(2)).save(any(), any(), any());
        verify(userRemoteWebClient, times(0)).deleteFile(null);
    }

    @Test
    void checkUpdateManualPaymentWithImage() {
        Employee employee = getEmployee();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        employee.setFirstName("Yuriy");
        employee.setLastName("Gerasum");
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        when(userRemoteWebClient.uploadFile(file)).thenReturn("path");
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY_UK + 1, "Yuriy" + "  " + "Gerasum",
            getOrder().getId());
        paymentServiceImpl.updateManualPayment(1L, getManualPaymentRequestDto(), file, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(2)).save(any(), any(), any());
    }

    @Test
    void checkManualPaymentNotFound() {
        Employee employee = getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        ManualPaymentRequestDto manualPaymentRequestDto = getManualPaymentRequestDto();
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> paymentServiceImpl.updateManualPayment(1L, manualPaymentRequestDto, null, "abc"));
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void updateManualPayment() {
        Employee employee = getEmployee();
        Order order = getOrderUserFirst();
        Payment payment = getPayment();
        ManualPaymentRequestDto requestDto = getManualPaymentRequestDto();
        requestDto.setImagePath("");
        payment.setImagePath("abc");
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());

        when(employeeRepository.findByUuid(employee.getUuid())).thenReturn(Optional.of(employee));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));

        paymentServiceImpl.updateManualPayment(payment.getId(), requestDto, file, employee.getUuid());

        verify(paymentRepository).save(any(Payment.class));
        verify(eventService, times(2)).save(any(), any(), any());
    }

    @Test
    void updateManualPaymentUserNotFoundExceptionTest() {
        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
            () -> paymentServiceImpl.updateManualPayment(1L, null, null, "abc"));
    }

    @Test
    void updateManualPaymentWebClientRequestExceptionTest() {
        LogCaptor logCaptor = LogCaptor.forClass(PaymentServiceImpl.class);
        Employee employee = getEmployee();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        ManualPaymentRequestDto requestDto = getManualPaymentRequestDto();
        employee.setFirstName("Yuriy");
        employee.setLastName("Gerasum");
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        when(userRemoteWebClient.uploadFile(any()))
            .thenThrow(webClientRequestException);
        paymentServiceImpl.updateManualPayment(1L, requestDto, file, "abc");
        List<String> warns = logCaptor.getWarnLogs();
        assertTrue(warns.getFirst().contains("User service is unavailable: null"));
    }

    @Test
    void updateManualPaymentDeleteImageWebClientRequestExceptionTest() {
        LogCaptor logCaptor = LogCaptor.forClass(PaymentServiceImpl.class);
        Employee employee = getEmployee();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        ManualPaymentRequestDto requestDto = getManualPaymentRequestDtoWithoutImage();
        employee.setFirstName("Yuriy");
        employee.setLastName("Gerasum");
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        doThrow(webClientRequestException)
            .when(userRemoteWebClient).deleteFile(anyString());
        paymentServiceImpl.updateManualPayment(1L, requestDto, file, "abc");
        List<String> warns = logCaptor.getWarnLogs();
        assertTrue(warns.getFirst().contains("User service is unavailable: null"));
    }

    @Test
    void updateManualPaymentPaymentNotFoundExceptionTest() {
        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.of(getEmployee()));
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> paymentServiceImpl.updateManualPayment(1L, null, null, "abc"));
    }

    @ParameterizedTest
    @MethodSource("provideManualPaymentRequestDto")
    void saveNewManualPayment(ManualPaymentRequestDto paymentDetails, MultipartFile image) {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenReturn(payment);
        doNothing().when(eventService).save(anyString(), anyString(), any(Long.class));

        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, image, TEST_EMAIL);

        verify(eventService, times(1))
            .save(OrderHistory.ORDER_PAID_UK, OrderHistory.SYSTEM_UK, order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentBuildResponseWebClientRequestExceptionTest() {
        LogCaptor logCaptor = LogCaptor.forClass(PaymentServiceImpl.class);
        ManualPaymentRequestDto requestDto = getManualPaymentRequestDto();
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(userRemoteWebClient.uploadFile(any()))
            .thenThrow(webClientRequestException);
        doNothing().when(eventService).save(anyString(), anyString(), any(Long.class));

        paymentServiceImpl.saveNewManualPayment(1L, requestDto, file, TEST_EMAIL);
        List<String> warns = logCaptor.getWarnLogs();
        assertTrue(warns.getFirst().contains("User service is unavailable: null"));
    }

    @Test
    void saveNewManualPaymentWithZeroAmount() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        order.setPointsToUse(0);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Payment payment = getManualPayment();
        payment.setAmount(0L);
        order.setPayment(singletonList(payment));
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(0L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1,
            "Петро  Петренко", order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithHalfPaidAmount() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        order.setPointsToUse(0);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
        Payment payment = getManualPayment();
        payment.setAmount(50_00L);
        order.setPayment(singletonList(payment));
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(50_00L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        when(orderBagService.findAllBagsInOrderBagsList(anyList())).thenReturn(ModelUtils.TEST_BAG_LIST2);

        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);

        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1,
            "Петро  Петренко", order.getId());
        verify(eventService, times(1))
            .save(OrderHistory.ORDER_HALF_PAID_UK, OrderHistory.SYSTEM_UK, order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPaidAmount() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Payment payment = getManualPayment();
        payment.setAmount(500_00L);
        order.setPayment(singletonList(payment));
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500_00L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1,
            "Петро  Петренко", order.getId());
        verify(eventService, times(1))
            .save(OrderHistory.ORDER_PAID_UK, OrderHistory.SYSTEM_UK, order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPaidOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1, "Петро" + "  " + "Петренко",
            order.getId());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPartiallyPaidOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(200L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1, "Петро" + "  " + "Петренко",
            order.getId());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithUnpaidOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1, "Петро" + "  " + "Петренко",
            order.getId());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPaymentRefundedOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1, "Петро" + "  " + "Петренко",
            order.getId());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL);
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWhenImageNotNull() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY_UK + 1, "Петро" + "  " + "Петренко",
            order.getId());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, mock(MultipartFile.class),
            TEST_EMAIL);

        verify(eventService, times(1))
            .save("Замовлення Оплачено", "Система", order.getId());
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithoutLinkAndImageTest() {
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500L).paymentId("1").build();
        assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, TEST_EMAIL));
    }

    @Test
    void processRefundForBroughtItHimselfOrder_ShouldRefundMoney_andSaveRefund() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney();
        refundDto.setAmount(100L);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL);
        verify(refundRepository).save(any(Refund.class));
        verify(orderRepository).save(any(Order.class));
        verify(eventService).saveEvent(eq(OrderHistory.CANCELED_ORDER_MONEY_REFUND_UK), eq(TEST_EMAIL),
            any(Long.class));
        assertEquals(OrderPaymentStatus.PAYMENT_REFUNDED, order.getOrderPaymentStatus());
    }

    @Test
    void processRefundForOrder_ShouldReturnFalse() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney();
        refundDto.setAmount(100L);
        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertFalse(paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL));
    }

    @Test
    void processRefundForOrder_ShouldThrowBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney();
        refundDto.setReturnMoney(false);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL));
        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, order.getOrderStatus()), exception.getMessage());
    }

    @Test
    void processRefundForOrder_ValidateRefoundAmount_ShouldThrowBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        RefundDto refundDto = getRefundDto_ReturnMoney();
        refundDto.setAmount(99999L);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL));
        assertEquals(INVALID_REQUESTED_REFUND_AMOUNT, exception.getMessage());
    }

    @Test
    void processRefundForCanceledOrder_ShouldRefundMoney_andSaveRefund() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney();
        order.setOrderStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertTrue(paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL));
        verify(refundRepository).save(any(Refund.class));
        verify(orderRepository).save(any(Order.class));
        verify(eventService).saveEvent(eq(OrderHistory.CANCELED_ORDER_MONEY_REFUND_UK), eq(TEST_EMAIL),
            any(Long.class));
        assertEquals(OrderPaymentStatus.PAYMENT_REFUNDED, order.getOrderPaymentStatus());
    }

    @Test
    void processRefundForOrder_ShouldRefundMoney_andThrows_ORDER_HAS_NO_OVERPAYMENT() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney();
        order.setOrderStatus(OrderStatus.CANCELED);
        order.getPayment().forEach(p -> p.setAmount(-300L));
        order.setPointsToUse(1);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
                TEST_EMAIL));
        assertEquals(ORDER_HAS_NO_OVERPAYMENT, exception.getMessage());
    }

    @Test
    void processRefundForDoneOrder_ShouldRefundMoney_andThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
                TEST_EMAIL));
        assertEquals(INCOMPATIBLE_ORDER_STATUS_FOR_MONEY_REFUND, exception.getMessage());
    }

    @Test
    void processRefundForDoneOrder_ShouldRefundBonuses_andSaveOrder() {
        Order order = getOrderForGetOrderStatusData2Test();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());

        assertTrue(paymentServiceImpl.processRefundForOrder(order.getId(), getRefundDto_ReturnBonuses(),
            TEST_EMAIL));
        verify(orderRepository).save(any(Order.class));
        verify(userRepository).save(any(User.class));
        verify(eventService).saveEvent(eq(OrderHistory.ADDED_BONUSES_UK), eq(TEST_EMAIL), any(Long.class));
        assertEquals(OrderPaymentStatus.PAYMENT_REFUNDED, order.getOrderPaymentStatus());
    }

    @Test
    void processRefundForDoneOrder_ShouldRefundBonuses_andThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.getPayment().forEach(p -> p.setAmount(-300L));
        order.setPointsToUse(1);
        RefundDto refundDto = getRefundDto_ReturnBonuses();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
                TEST_EMAIL));
        assertEquals(ORDER_HAS_NO_OVERPAYMENT, exception.getMessage());
    }

    @Test
    void processRefundForCanceledOrder_ShouldThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoneyAndBonuses();
        order.setOrderStatus(OrderStatus.CANCELED);
        order.getPayment().forEach(p -> p.setAmount(-300L));
        order.setPointsToUse(1);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
                TEST_EMAIL));
        assertEquals(REFUND_CONFLICT_MONEY_AND_BONUSES, exception.getMessage());
    }

    @Test
    void processRefundForDoneOrder_ShouldThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), null,
                TEST_EMAIL));
        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, order.getOrderStatus()), exception.getMessage());
    }

    @Test
    void processRefundForBroughtItHimselfOrder_refundMoney_shouldThrowsBadRequestException1() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        RefundDto refundDto = getRefundDto_ReturnMoney();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
                TEST_EMAIL));
        assertEquals(INVALID_REQUESTED_REFUND_AMOUNT, exception.getMessage());
    }

    @Test
    void processRefundForBroughtItHimselfOrder_ShouldRefundBonuses() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnBonuses();
        refundDto.setAmount(100L);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertFalse(paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
            TEST_EMAIL));
        verify(userRepository).save(any(User.class));
        verify(orderRepository).save(any(Order.class));
        verify(eventService).saveEvent(eq(OrderHistory.ADDED_BONUSES_UK), eq(TEST_EMAIL),
            any(Long.class));
        assertEquals(OrderPaymentStatus.PAYMENT_REFUNDED, order.getOrderPaymentStatus());
    }

    @Test
    void processRefundForBroughtItHimselfOrder_refundBonuses_shouldThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_ReturnMoney().setAmount(100L);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(refundRepository.save(any(Refund.class))).thenThrow(new BadRequestException(CANNOT_REFUND_MONEY));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
                TEST_EMAIL));
        assertEquals(CANNOT_REFUND_MONEY, exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventService, never()).saveEvent(eq(OrderHistory.CANCELED_ORDER_MONEY_REFUND_UK), eq(TEST_EMAIL),
            any(Long.class));
    }

    @Test
    void processRefundForBroughtItHimselfOrder_nothingToRefund_shouldReturnFalse() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertFalse(paymentServiceImpl.processRefundForOrder(order.getId(), null,
            TEST_EMAIL));
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventService, never()).saveEvent(eq(OrderHistory.CANCELED_ORDER_MONEY_REFUND_UK), eq(TEST_EMAIL),
            any(Long.class));
        verify(userRepository, never()).save(any());
        verify(refundRepository, never()).save(any());
    }

    @Test
    void processRefundForBroughtItHimselfOrder_nothingToRefund_shouldReturnFalse2() {
        Order order = getOrderForGetOrderStatusData2Test();
        RefundDto refundDto = getRefundDto_NothingToRefund();
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertFalse(paymentServiceImpl.processRefundForOrder(order.getId(), refundDto,
            TEST_EMAIL));
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventService, never()).saveEvent(eq(OrderHistory.CANCELED_ORDER_MONEY_REFUND_UK), eq(TEST_EMAIL),
            any(Long.class));
        verify(userRepository, never()).save(any());
        verify(refundRepository, never()).save(any());
    }

    @Test
    void processRefundForOrderWhenOrderStatusNotHandled() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.FORMED);
        RefundDto refundDto = getRefundDto_ReturnMoney();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        boolean result = paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL);

        assertFalse(result);
        verify(refundRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(eventService, never()).saveEvent(anyString(), anyString(), any());
    }

    @Test
    void processPointsRefundForOrderWhenPointsToUseExists() {
        int pointsToUse = 100;
        int currentUserPoints = 200;

        List<ChangeOfPoints> changeOfPointsList = new ArrayList<>();
        User user = getTestUser();
        user.setCurrentPoints(currentUserPoints);
        user.setChangeOfPointsList(changeOfPointsList);
        Order order = getOrderForGetOrderStatusData2Test();
        order.setPointsToUse(pointsToUse);
        order.setUser(user);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        paymentServiceImpl.processPointsRefundForOrder(order.getId());

        verify(userRepository, times(1)).save(user);
        ChangeOfPoints changeOfPoints = user.getChangeOfPointsList().getFirst();
        assertEquals(pointsToUse, changeOfPoints.getAmount());
        assertEquals(LocalDateTime.now().toLocalDate(), changeOfPoints.getDate().toLocalDate());
        assertEquals(BonusReason.REFUND_CANCELED_ORDER, changeOfPoints.getReason());
        assertEquals(user, changeOfPoints.getUser());
        assertEquals(order, changeOfPoints.getOrder());

        paymentServiceImpl.processPointsRefundForOrder(order.getId());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void processPointsRefundForOrderWhenCurrentPointsIsNull() {
        int pointsToUse = 100;

        User user = Mockito.spy(User.class);
        List<ChangeOfPoints> changeOfPointsList = mock(ArrayList.class);
        Order order = getOrderForGetOrderStatusData2Test();
        order.setPointsToUse(pointsToUse);
        order.setUser(user);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(user.getChangeOfPointsList()).thenReturn(changeOfPointsList);

        paymentServiceImpl.processPointsRefundForOrder(order.getId());

        verify(user).setCurrentPoints(0);
        verify(user).setCurrentPoints(pointsToUse);
        verify(userRepository).save(user);
    }

    @Test
    void processPointsRefundForOrderWhenListIsNull() {
        int pointsToUse = 100;
        int currentUserPoints = 200;

        User user = Mockito.spy(User.class);
        Order order = getOrderForGetOrderStatusData2Test();
        order.setPointsToUse(pointsToUse);
        order.setUser(user);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(user.getCurrentPoints()).thenReturn(currentUserPoints);

        paymentServiceImpl.processPointsRefundForOrder(order.getId());

        verify(user).setChangeOfPointsList(any(ArrayList.class));
        verify(userRepository).save(user);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0})
    void processPointsRefundForOrderWhenPointsToUseIsInvalid(Integer pointsToUse) {
        Order orderWithNullPoints = getOrderForGetOrderStatusData2Test();
        orderWithNullPoints.setPointsToUse(pointsToUse);

        when(orderRepository.findById(orderWithNullPoints.getId())).thenReturn(Optional.of(orderWithNullPoints));

        paymentServiceImpl.processPointsRefundForOrder(orderWithNullPoints.getId());
        verify(userRepository, never()).save(any());
    }

    @Test
    void processRefundForDoneOrderWhenNeitherMoneyNorBonusesSelected() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.DONE);
        RefundDto refundDto = getRefundDto_NothingToRefund();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL));

        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, order.getOrderStatus()), exception.getMessage());
        verify(refundRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(eventService, never()).saveEvent(anyString(), anyString(), any());
    }

    @Test
    void processRefundForBroughtItHimselfOrderWhenRefundingBonuses() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        order.setPointsToUse(50);

        Payment payment = getPayment();
        payment.setAmount(100L);
        payment.setPaymentStatus(PaymentStatus.PAID);
        order.setPayment(Collections.singletonList(payment));

        RefundDto refundDto = getRefundDto_ReturnBonuses();
        refundDto.setAmount(12000L);

        User user = getTestUser();
        order.setUser(user);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.processRefundForOrder(order.getId(), refundDto, TEST_EMAIL));

        assertEquals(INVALID_REQUESTED_REFUND_AMOUNT, exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void saveNewManualPaymentWhenEmployeeCannotAccessOrder() {
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);

        Employee employee = getEmployee();
        ManualPaymentRequestDto paymentRequestDto = getManualPaymentRequestDto();
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(tariffsInfo.getId(), employee.getId()))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.saveNewManualPayment(1L, paymentRequestDto, image, TEST_EMAIL));

        assertEquals(ErrorMessage.CANNOT_ACCESS_ORDER_FOR_EMPLOYEE + order.getId(), exception.getMessage());
        verify(paymentRepository, never()).save(any());
        verify(eventService, never()).save(anyString(), anyString(), any());
    }

    private static Stream<Arguments> provideManualPaymentRequestDto() {
        return Stream.of(Arguments.of(ManualPaymentRequestDto.builder()
            .settlementDate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build(), null),
            Arguments.of(ManualPaymentRequestDto.builder()
                .settlementDate("02-08-2021").amount(500L).imagePath("path").paymentId("1").build(),
                mock(MultipartFile.class)));
    }
}