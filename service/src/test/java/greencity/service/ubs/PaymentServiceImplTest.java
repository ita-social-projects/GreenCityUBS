package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.OrderHistory;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.locations.LocationApiService;
import greencity.service.notification.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.stream.Stream;
import static greencity.ModelUtils.*;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock(lenient = true)
    OrderAddressRepository orderAddressRepository;
    @Mock
    private FileService fileService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CertificateRepository certificateRepository;

    @Mock(lenient = true)
    private ModelMapper modelMapper;

    @Mock
    private ReceivingStationRepository receivingStationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmployeeOrderPositionRepository employeeOrderPositionRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BagRepository bagRepository;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock(lenient = true)
    private NotificationServiceImpl notificationService;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private UBSManagementServiceImpl ubsManagementService;

    @Mock
    private EventService eventService;

    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;

    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;

    @Mock
    private UBSClientServiceImpl ubsClientService;

    @Mock
    private UBSManagementServiceImpl ubsManagementServiceMock;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    OrdersAdminsPageService ordersAdminsPageService;

    @Mock
    TariffsInfoRepository tariffsInfoRepository;

    @Mock
    private LocationApiService locationApiService;

    @Mock
    RefundRepository refundRepository;
    @Mock
    private OrderBagService orderBagService;
    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;
    @Mock
    private PaymentUtil paymentUtil;
    @Mock
    private OrderBagRepository orderBagRepository;
    @Mock
    private OrderLockService orderLockService;

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
        doNothing().when(fileService).delete("");
        doNothing().when(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + getManualPayment().getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder());
        paymentServiceImpl.deleteManualPayment(1L, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).deletePaymentById(1L);
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
        verify(fileService).delete(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + payment.getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder());

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
        verify(fileService, times(0)).delete(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + payment.getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder());
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
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY + 1,
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder());
        paymentServiceImpl.updateManualPayment(1L, getManualPaymentRequestDto(), null, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(2)).save(any(), any(), any());
        verify(fileService, times(0)).delete(null);
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
        when(fileService.upload(file)).thenReturn("path");
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY + 1, "Yuriy" + "  " + "Gerasum",
            getOrder());
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
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenReturn(payment);
        doNothing().when(eventService).save(anyString(), anyString(), any(Order.class));

        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, image, "test@gmail.com");

        verify(eventService, times(1))
            .save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    private static Stream<Arguments> provideManualPaymentRequestDto() {
        return Stream.of(Arguments.of(ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build(), null),
            Arguments.of(ManualPaymentRequestDto.builder()
                .settlementdate("02-08-2021").amount(500L).imagePath("path").paymentId("1").build(),
                Mockito.mock(MultipartFile.class)));
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
            .settlementdate("02-08-2021").amount(0L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1,
            "Петро  Петренко", order);
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
            .settlementdate("02-08-2021").amount(50_00L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        when(orderBagService.findAllBagsInOrderBagsList(anyList())).thenReturn(ModelUtils.TEST_BAG_LIST2);

        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");

        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1,
            "Петро  Петренко", order);
        verify(eventService, times(1))
            .save(OrderHistory.ORDER_HALF_PAID, OrderHistory.SYSTEM, order);
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
            .settlementdate("02-08-2021").amount(500_00L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1,
            "Петро  Петренко", order);
        verify(eventService, times(1))
            .save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
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
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
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
            .settlementdate("02-08-2021").amount(200L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
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
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
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
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
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
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, Mockito.mock(MultipartFile.class),
            "test@gmail.com");

        verify(eventService, times(1))
            .save("Замовлення Оплачено", "Система", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithoutLinkAndImageTest() {
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).paymentId("1").build();
        assertThrows(BadRequestException.class,
            () -> paymentServiceImpl.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com"));
    }
}