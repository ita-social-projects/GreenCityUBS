package greencity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.SortingOrder;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.*;
import greencity.service.ubs.*;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UBSManagementServiceImplTest {
    @Mock
    AddressRepository addressRepository;
    double distance = 2;
    int litres = 1000;

    @Mock
    private FileService fileService;

    @Mock(lenient = true)
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CertificateRepository certificateRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ViolationRepository violationRepository;

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
    private BagTranslationRepository bagTranslationRepository;

    @Mock
    private RestClient restClient;

    @Mock(lenient = true)
    private NotificationServiceImpl notificationService;

    @Mock
    private BagsInfoRepo bagsInfoRepo;

    @Mock
    private AdditionalBagsInfoRepo additionalBagsInfoRepo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UpdateOrderDetail updateOrderRepository;

    @InjectMocks
    private UBSManagementServiceImpl ubsManagementService;

    @Mock
    private EventService eventService;

    @Mock
    LanguageRepository languageRepository;

    @Mock
    private CertificateCriteriaRepo certificateCriteriaRepo;

    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;

    @Mock
    private CustomTableViewRepo customTableViewRepo;

    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;

    @Mock
    private UBSClientServiceImpl ubsClientService;

    @Mock
    private UBSManagementServiceImpl ubsManagementServiceMock;
    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BigOrderTableRepository bigOrderTableRepository;

    @Mock
    OrdersAdminsPageService ordersAdminsPageService;

    private void getMocksBehavior() {

        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(25);

        for (Coordinates coordinate : ModelUtils.getCoordinatesSet()) {
            List<Order> orders = ModelUtils.getOrdersToGroupThem().stream()
                .filter(e -> e.getUbsUser().getAddress().getCoordinates().equals(coordinate)).collect(
                    Collectors.toList());
            when(orderRepository.undeliveredOrdersGroupThem(coordinate.getLatitude(), coordinate.getLongitude()))
                .thenReturn(orders);
            for (Order order : orders) {
                when(modelMapper.map(order, OrderDto.class)).thenReturn(OrderDto.builder()
                    .latitude(order.getUbsUser().getAddress().getCoordinates().getLatitude())
                    .longitude(order.getUbsUser().getAddress().getCoordinates().getLongitude())
                    .build());
            }
        }
    }

    @Test
    void getClusteredCoordsTest() {
        when(addressRepository.undeliveredOrdersCoordsWithCapacityLimit(litres))
            .thenReturn(ModelUtils.getCoordinatesSet());
        getMocksBehavior();
        List<GroupedOrderDto> expected = ModelUtils.getGroupedOrders();
        List<GroupedOrderDto> actual = ubsManagementService.getClusteredCoords(distance, litres);
        assertEquals(expected, actual);
    }

    @Test
    void getClusteredCoordsWithBiggerClusterLitresTest() {
        when(addressRepository.undeliveredOrdersCoordsWithCapacityLimit(60)).thenReturn(ModelUtils.getCoordinatesSet());
        getMocksBehavior();
        List<GroupedOrderDto> expected = ModelUtils.getGroupedOrdersFor60LitresLimit();
        List<GroupedOrderDto> actual = ubsManagementService.getClusteredCoords(distance, 60);

        assertEquals(expected, actual);
    }

    @Test
    void addCertificateTest() {
        CertificateDtoForAdding certificateDtoForAdding = new CertificateDtoForAdding("1111-1234", 5, 100);
        Certificate certificate = new Certificate();
        when(modelMapper.map(certificateDtoForAdding, Certificate.class)).thenReturn(certificate);
        ubsManagementService.addCertificate(certificateDtoForAdding);
        verify(certificateRepository, times(1)).save(certificate);
    }

    @Test
    void getAllCertificates() {
        Pageable pageable =
            PageRequest.of(0, 5, Sort.by(Sort.Direction.fromString(SortingOrder.DESC.toString()), "points"));
        CertificateDtoForSearching certificateDtoForSearching = ModelUtils.getCertificateDtoForSearching();
        List<Certificate> certificates =
            Collections.singletonList(ModelUtils.getCertificate());
        List<CertificateDtoForSearching> certificateDtoForSearchings =
            Collections.singletonList(certificateDtoForSearching);
        PageableDto<CertificateDtoForSearching> certificateDtoForSearchingPageableDto =
            new PageableDto<>(certificateDtoForSearchings, certificateDtoForSearchings.size(), 0, 1);
        Page<Certificate> certificates1 = new PageImpl<>(certificates, pageable, certificates.size());
        when(modelMapper.map(certificates.get(0), CertificateDtoForSearching.class))
            .thenReturn(certificateDtoForSearching);
        when(certificateRepository.getAll(pageable)).thenReturn(certificates1);
        PageableDto<CertificateDtoForSearching> actual =
            ubsManagementService.getAllCertificates(pageable, "points", SortingOrder.DESC);
        assertEquals(certificateDtoForSearchingPageableDto, actual);
    }

    @Test
    void checkOrderNotFound() {
        Assertions.assertThrows(NotFoundOrderAddressException.class, () -> {
            ubsManagementService.getAddressByOrderId(10000000l);
        });
    }

    @Test
    void returnsViolationDetailsByOrderId() {
        Violation violation = ModelUtils.getViolation();
        Optional<ViolationDetailInfoDto> expected = Optional.of(ModelUtils.getViolationDetailInfoDto());
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.of(violation.getOrder().getUser()));
        when(violationRepository.findByOrderId(1L)).thenReturn(Optional.of(violation));
        Optional<ViolationDetailInfoDto> actual = ubsManagementService.getViolationDetailsByOrderId(1L);

        assertEquals(expected, actual);
    }

    @Test
    void checkPaymentNotFound() {
        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.getOrderDetailStatus(100L);
        });
    }

    @Test
    void returnExportDetailsByOrderId() {
        ExportDetailsDto expected = ModelUtils.getExportDetails();
        Order order = ModelUtils.getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<ReceivingStation> stations = Arrays.asList(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        assertEquals(expected, ubsManagementService.getOrderExportDetails(1L));
    }

    @Test
    void updateExportDetailsByOrderId() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        ExportDetailsDtoUpdate dto = ModelUtils.getExportDetailsRequest();
        Order order = ModelUtils.getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<ReceivingStation> stations = Arrays.asList(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetails(order.getId(), dto, "abc");

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateExportDetailsNotSuccessfulByOrderId() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        ExportDetailsDtoUpdate dto = ModelUtils.getExportDetailsRequest();
        Order order = ModelUtils.getOrderExportDetailsWithNullValues();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<ReceivingStation> stations = Arrays.asList(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetails(order.getId(), dto, "abc");

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void checkStationNotFound() {
        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.getOrderExportDetails(100L);
        });
    }

    @Test
    void deleteViolationThrowsException() {
        assertThrows(UserNotFoundException.class, () -> ubsManagementService.deleteViolation(1L, "abc"));
    }

    @Test
    void deleteViolationFromOrderResponsesNotFoundWhenNoViolationInOrder() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(violationRepository.findByOrderId(1l)).thenReturn(Optional.empty());
        Assertions.assertThrows(UnexistingOrderException.class, () -> ubsManagementService.deleteViolation(1L, "abc"));
        verify(violationRepository, times(1)).findByOrderId(1L);
    }

    @Test
    void deleteViolationFromOrderByOrderId() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        Violation violation = ModelUtils.getViolation2();
        Long id = ModelUtils.getViolation().getOrder().getId();
        when(violationRepository.findByOrderId(1l)).thenReturn(Optional.of(violation));
        doNothing().when(violationRepository).deleteById(id);
        ubsManagementService.deleteViolation(id, "abc");

        verify(violationRepository, times(1)).deleteById(id);
    }

    @Test
    void saveNewManualPayment() {
        User user = ModelUtils.getTestUser();
        user.setRecipientName("Yuriy");
        user.setRecipientSurname("Gerasum");
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        Order order = ModelUtils.getFormedOrder();
        Payment payment = ModelUtils.getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Yuriy" + "  " + "Gerasum", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "abc");

        verify(eventService, times(1))
            .save("Замовлення Оплачено", "Система", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1l);
    }

    @Test
    void checkDeleteManualPayment() {
        User user = ModelUtils.getTestUser();
        Order order = ModelUtils.getFormedOrder();
        user.setRecipientName("Yuriy");
        user.setRecipientSurname("Gerasum");
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1l)).thenReturn(Optional.of(getManualPayment()));
        doNothing().when(paymentRepository).deletePaymentById(1l);
        doNothing().when(fileService).delete("");
        doNothing().when(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + 1, "Yuriy" + "  " + "Gerasum",
            getOrder());
        ubsManagementService.deleteManualPayment(1l, "abc");
        verify(paymentRepository, times(1)).findById(1l);
        verify(paymentRepository, times(1)).deletePaymentById(1l);
    }

    @Test
    void checkUpdateManualPayment() {
        User user = ModelUtils.getTestUser();
        Order order = ModelUtils.getFormedOrder();
        user.setRecipientName("Yuriy");
        user.setRecipientSurname("Gerasum");
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY + 1, "Yuriy" + "  " + "Gerasum",
            getOrder());
        ubsManagementService.updateManualPayment(1L, getManualPaymentRequestDto(), null, "abc");
        verify(paymentRepository, times(1)).findById(1l);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(1)).save(any(), any(), any());
        verify(fileService, times(0)).delete(null);
    }

    @Test
    void checkUpdateManualPaymentWithImage() {
        User user = ModelUtils.getTestUser();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        user.setRecipientName("Yuriy");
        user.setRecipientSurname("Gerasum");
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        when(fileService.upload(file)).thenReturn("path");
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY + 1, "Yuriy" + "  " + "Gerasum",
            getOrder());
        ubsManagementService.updateManualPayment(1L, getManualPaymentRequestDto(), file, "abc");
        verify(paymentRepository, times(1)).findById(1l);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(1)).save(any(), any(), any());
    }

    @Test
    void checkManualPaymentNotFound() {
        User user = ModelUtils.getTestUser();
        user.setRecipientName("Yuriy");
        user.setRecipientSurname("Gerasum");
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        ManualPaymentRequestDto manualPaymentRequestDto = getManualPaymentRequestDto();
        when(paymentRepository.findById(1l)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1l, manualPaymentRequestDto, null, "abc"));
        verify(paymentRepository, times(1)).findById(1l);
    }

    @Test
    void checkReturnOverpaymentInfo() {
        Order order = ModelUtils.getOrder();
        when(orderRepository.getUserByOrderId(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        order.setUser(ModelUtils.getUser());

        Long sumToPay = 0L;

        assertEquals("Зарахування на бонусний рахунок", ubsManagementService.returnOverpaymentInfo(1L, sumToPay, 0L)
            .getPaymentInfoDtos().get(1).getComment());

        assertEquals(0L, ubsManagementService.returnOverpaymentInfo(1L, sumToPay, 1L)
            .getOverpayment());
        assertEquals(AppConstant.PAYMENT_REFUND,
            ubsManagementService.returnOverpaymentInfo(order.getId(), sumToPay, 1L).getPaymentInfoDtos().get(1)
                .getComment());
    }

    @Test
    void checkReturnOverpaymentThroweException() {
        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.returnOverpaymentInfo(100L, 1L, 1L);
        });
    }

    @Test
    void checkReturnOverpaymentThroweExceptioninGetPaymentInfo() {
        Order order = ModelUtils.getOrder();
        when(orderRepository.getUserByOrderId(1L)).thenReturn(Optional.of(order));

        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.returnOverpaymentInfo(1L, 1L, 1L);
        });
    }

    @Test
    void checkGetPaymentInfo() {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(100L, ubsManagementService.getPaymentInfo(order.getId(), 100L).getOverpayment());
        assertEquals(200L, ubsManagementService.getPaymentInfo(order.getId(), 100L).getPaidAmount());
        assertEquals(0L, ubsManagementService.getPaymentInfo(order.getId(), 100L).getUnPaidAmount());
    }

    @Test
    void checkReturnOverpaymentForStatusDone() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.DONE);
        OverpaymentInfoRequestDto dto = ModelUtils.getOverpaymentInfoRequestDto();
        dto.setBonuses(0L);
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        ubsManagementService.returnOverpayment(order.getId(), dto, "abc");
        assertEquals(2L, order.getPayment().size());
        assertEquals(2L, user.getChangeOfPointsList().size());
        assertEquals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT,
            order.getPayment().get(order.getPayment().size() - 1).getComment());
        assertEquals(dto.getOverpayment(), user.getCurrentPoints().longValue());
        assertEquals(dto.getOverpayment(), order.getPayment().get(order.getPayment().size() - 1).getAmount());
    }

    @Test
    void returnOverpaymentThrowsException() {
        OverpaymentInfoRequestDto dto = ModelUtils.getOverpaymentInfoRequestDto();
        dto.setBonuses(0L);
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.DONE);
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.empty());

        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.returnOverpayment(1L, dto, "abc"));
    }

    @Test
    void returnOverpaymentAsMoneyForStatusCancelled() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.CANCELED);
        OverpaymentInfoRequestDto dto = ModelUtils.getOverpaymentInfoRequestDto();
        dto.setComment(AppConstant.PAYMENT_REFUND);
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        ubsManagementService.returnOverpayment(order.getId(), dto, "abc");
        assertEquals(2L, user.getChangeOfPointsList().size());
        assertEquals(AppConstant.PAYMENT_REFUND,
            order.getPayment().get(order.getPayment().size() - 1).getComment());
        assertEquals(dto.getBonuses(), user.getCurrentPoints().longValue());
        assertEquals(dto.getOverpayment(), order.getPayment().get(order.getPayment().size() - 1).getAmount());
    }

    @Test
    void returnOverpaymentAsBonusesForStatusCancelled() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.CANCELED);
        OverpaymentInfoRequestDto dto = ModelUtils.getOverpaymentInfoRequestDto();
        dto.setComment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT);
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        ubsManagementService.returnOverpayment(order.getId(), dto, "abc");
        assertEquals(3L, user.getChangeOfPointsList().size());
        assertEquals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT,
            order.getPayment().get(order.getPayment().size() - 1).getComment());
        assertEquals(dto.getOverpayment(), order.getPayment().get(order.getPayment().size() - 1).getAmount());
        assertEquals(dto.getBonuses() + dto.getOverpayment(), user.getCurrentPoints().longValue());
    }

    @Test
    void updateOrderDetailStatusThrowException() {

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrder()));
        assertThrows(PaymentNotFoundException.class, () -> {
            ubsManagementService.updateOrderDetailStatus(1L, ModelUtils.getTestOrderDetailStatusRequestDto(), "uuid");
        });
    }

    @Test
    void updateOrderDetailStatus() {
        User user = ModelUtils.getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = user.getOrders().get(0);
        order.setOrderDate((LocalDateTime.of(2021, 5, 15, 10, 20, 5)));

        List<Payment> payment = new ArrayList<>();
        payment.add(Payment.builder().build());

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.paymentInfo(anyLong())).thenReturn(payment);
        when(paymentRepository.saveAll(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenReturn(order);

        OrderDetailStatusRequestDto testOrderDetail = ModelUtils.getTestOrderDetailStatusRequestDto();
        OrderDetailStatusDto expectedObject = ModelUtils.getTestOrderDetailStatusDto();
        OrderDetailStatusDto producedObject = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");
        assertEquals(expectedObject.getOrderStatus(), producedObject.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObject.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObject.getDate());
        testOrderDetail.setOrderStatus(OrderStatus.ADJUSTMENT.toString());
        expectedObject.setOrderStatus(OrderStatus.ADJUSTMENT.toString());
        OrderDetailStatusDto producedObjectAdjustment = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectAdjustment.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectAdjustment.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectAdjustment.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.CONFIRMED.toString());
        expectedObject.setOrderStatus(OrderStatus.CONFIRMED.toString());
        OrderDetailStatusDto producedObjectConfirmed = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectConfirmed.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectConfirmed.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectConfirmed.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.NOT_TAKEN_OUT.toString());
        expectedObject.setOrderStatus(OrderStatus.NOT_TAKEN_OUT.toString());
        OrderDetailStatusDto producedObjectNotTakenOut = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectNotTakenOut.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectNotTakenOut.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectNotTakenOut.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.DONE.toString());
        expectedObject.setOrderStatus(OrderStatus.DONE.toString());
        OrderDetailStatusDto producedObjectDone = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectDone.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectDone.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectDone.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF.toString());
        expectedObject.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF.toString());
        OrderDetailStatusDto producedObjectBroughtItHimself = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectBroughtItHimself.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectBroughtItHimself.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectBroughtItHimself.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.ON_THE_ROUTE.toString());
        expectedObject.setOrderStatus(OrderStatus.ON_THE_ROUTE.toString());
        OrderDetailStatusDto producedObjectOnTheRoute = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectOnTheRoute.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectOnTheRoute.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectOnTheRoute.getDate());

        verify(eventService, times(1))
            .save("Статус Замовлення - Узгодження",
                user.getRecipientName() + "  " + user.getRecipientSurname(), order);
        verify(eventService, times(1))
            .save("Статус Замовлення - Підтверджено",
                user.getRecipientName() + "  " + user.getRecipientSurname(), order);
        verify(eventService, times(1))
            .save("Статус Замовлення - Скасовано" + "  " + order.getCancellationComment(),
                user.getRecipientName() + "  " + user.getRecipientSurname(), order);
    }

    @Test
    void getAllEmployeesByPosition() {
        Order order = ModelUtils.getOrder();
        EmployeePositionDtoRequest dto = ModelUtils.getEmployeePositionDtoRequest();
        List<EmployeeOrderPosition> newList = new ArrayList<>();
        newList.add(ModelUtils.getEmployeeOrderPosition());

        List<Position> positionList = new ArrayList<>();
        positionList.add(ModelUtils.getPosition());

        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(ModelUtils.getEmployee());
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.findAllByOrderId(anyLong())).thenReturn(newList);
        when(positionRepository.findAll()).thenReturn(positionList);
        when(employeeRepository.getAllEmployeeByPositionId(anyLong())).thenReturn(employeeList);
        assertEquals(dto, ubsManagementService.getAllEmployeesByPosition(order.getId()));
    }

    @Test
    void testUpdateAddress() {
        User user = ModelUtils.getTestUser();
        Address address = TEST_ADDRESS;
        address.setId(1L);
        OrderAddressExportDetailsDtoUpdate dtoUpdate = ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(TEST_ORDER));
        when(addressRepository.findById(dtoUpdate.getAddressId())).thenReturn(Optional.of(address));

        when(addressRepository.save(TEST_ADDRESS)).thenReturn(TEST_ADDRESS);
        when(modelMapper.map(TEST_ADDRESS, OrderAddressDtoResponse.class)).thenReturn(TEST_ORDER_ADDRESS_DTO_RESPONSE);
        Optional<OrderAddressDtoResponse> actual =
            ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, 1L, "abc");
        assertEquals(Optional.of(TEST_ORDER_ADDRESS_DTO_RESPONSE), actual);
        verify(orderRepository).findById(1L);
        verify(addressRepository).save(TEST_ADDRESS);
        verify(addressRepository).findById(TEST_ADDRESS.getId());
        verify(modelMapper).map(TEST_ADDRESS, OrderAddressDtoResponse.class);
    }

    @Test
    void testUpdateAddressThrowsOrderNotFoundException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, 1L, "abc"));
    }

    @Test
    void testUpdateAddressThrowsNotFoundOrderAddressException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrderWithoutAddress()));

        assertThrows(NotFoundOrderAddressException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, 1L, "abc"));
    }

    @Test
    void testGetOrderDetailStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(paymentRepository.paymentInfo(1L)).thenReturn(TEST_PAYMENT_LIST);

        OrderDetailStatusDto actual = ubsManagementService.getOrderDetailStatus(1L);

        assertEquals(ORDER_DETAIL_STATUS_DTO.getOrderStatus(), actual.getOrderStatus());
        assertEquals(ORDER_DETAIL_STATUS_DTO.getPaymentStatus(), actual.getPaymentStatus());
        assertEquals(ORDER_DETAIL_STATUS_DTO.getDate(), actual.getDate());

        verify(orderRepository).findById(1L);
        verify(paymentRepository).paymentInfo(1L);
    }

    @Test
    void testGetOrderDetailStatusThrowsUnExistingOrderException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.getOrderDetailStatus(1L));
    }

    @Test
    void testGetOrderDetailStatusThrowsPaymentNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(paymentRepository.paymentInfo(1)).thenReturn(Collections.emptyList());

        assertThrows(PaymentNotFoundException.class,
            () -> ubsManagementService.getOrderDetailStatus(1L));
    }

    @Test
    void testGetOrderDetails() {
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(TEST_ORDER));
        when(modelMapper.map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType())).thenReturn(TEST_BAG_MAPPING_DTO_LIST);
        when(bagRepository.findBagByOrderId(1L)).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(TEST_BAG, BagInfoDto.class)).thenReturn(TEST_BAG_INFO_DTO);
        when(bagTranslationRepository.findAllByLanguageOrder("ua", 1L)).thenReturn(TEST_BAG_TRANSLATION_LIST);
        when(modelMapper.map(TEST_BAG_TRANSLATION, BagTransDto.class)).thenReturn(TEST_BAG_TRANS_DTO);
        when(modelMapper.map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()))).thenReturn(TEST_ORDER_DETAILS_INFO_DTO_LIST);

        List<OrderDetailInfoDto> actual = ubsManagementService.getOrderDetails(1L, "ua");

        assertEquals(TEST_ORDER_DETAILS_INFO_DTO_LIST, actual);

        verify(orderRepository).getOrderDetails(1L);
        verify(modelMapper).map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType());
        verify(bagRepository).findBagByOrderId(1L);
        verify(modelMapper).map(TEST_BAG, BagInfoDto.class);
        verify(bagTranslationRepository).findAllByLanguageOrder("ua", 1L);
        verify(modelMapper).map(TEST_BAG_TRANSLATION, BagTransDto.class);
        verify(modelMapper).map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()));
    }

    @Test
    void testGetOrdersBagsDetails() {
        List<DetailsOrderInfoDto> detailsOrderInfoDtoList = new ArrayList<>();
        Map<String, Object> mapOne = Map.of("One", "Two");
        Map<String, Object> mapTwo = Map.of("One", "Two");
        List<Map<String, Object>> mockBagInfoRepository = Arrays.asList(mapOne, mapTwo);
        when(bagsInfoRepo.getBagInfo(1L)).thenReturn(mockBagInfoRepository);
        for (Map<String, Object> map : mockBagInfoRepository) {
            when(objectMapper.convertValue(map, DetailsOrderInfoDto.class)).thenReturn(getTestDetailsOrderInfoDto());
            detailsOrderInfoDtoList.add(getTestDetailsOrderInfoDto());
        }
        assertEquals(detailsOrderInfoDtoList.toString(),
            ubsManagementService.getOrderBagsDetails(1L).toString());
    }

    @Test
    void testSendNotificationAboutViolationWithFoundOrder() {
        AddingViolationsToUserDto addingViolationsToUserDto =
            new AddingViolationsToUserDto(1L, "violation", "LOW");
        Order order = GET_ORDER_DETAILS;
        when(orderRepository.findById(addingViolationsToUserDto.getOrderID())).thenReturn(Optional.of(order));
        UserViolationMailDto mailDto =
            new UserViolationMailDto(order.getUser().getRecipientName(), order.getUser().getRecipientEmail(), "ua",
                addingViolationsToUserDto.getViolationDescription());
        ubsManagementService.sendNotificationAboutViolation(addingViolationsToUserDto, "ua");
        verify(restClient, times(1)).sendViolationOnMail(mailDto);
    }

    @Test
    void testSendNotificationAboutViolationWithoutOrder() {
        AddingViolationsToUserDto addingViolationsToUserDto =
            new AddingViolationsToUserDto();
        when(orderRepository.findById(addingViolationsToUserDto.getOrderID())).thenReturn(Optional.empty());
        ubsManagementService.sendNotificationAboutViolation(addingViolationsToUserDto, "ua");
        verify(restClient, times(0)).sendViolationOnMail(new UserViolationMailDto());
    }

    @Test
    void testGetOrderExportDetailsReceivingStationNotFoundExceptionThrown() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(ModelUtils.getOrder()));
        List<ReceivingStation> receivingStations = new ArrayList<>();
        when(receivingStationRepository.findAll())
            .thenReturn(receivingStations);
        assertThrows(ReceivingStationNotFoundException.class,
            () -> ubsManagementService.getOrderExportDetails(1L));
    }

    @Test
    void testGetOrderDetailsThrowsException() {
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.empty());

        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.getOrderDetails(1L, "ua"));
    }

    @Test
    void checkGetAllUserViolations() {
        User user = ModelUtils.getUser();
        user.setUuid(restClient.findUuidByEmail(user.getRecipientEmail()));
        user.setViolations(1);

        ViolationsInfoDto expected = modelMapper.map(user, ViolationsInfoDto.class);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.ofNullable(user));

        ViolationsInfoDto actual = ubsManagementService.getAllUserViolations(user.getRecipientEmail());

        assertEquals(expected, actual);
    }

    @Test
    void checkAddUserViolation() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setUser(user);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.ofNullable(order));
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(1);
        ubsManagementService.addUserViolation(add, new MultipartFile[2], "abc");

        assertEquals(1, user.getViolations());
    }

    @Test
    void getClusteredCoordsAlongWithSpecifiedTest() {
        Coordinates coord = ModelUtils.getCoordinates();
        Set<Coordinates> result = new HashSet<>();
        result.add(coord);
        List<Order> orderList = new ArrayList<>();
        orderList.add(ModelUtils.getOrderTest());
        when(addressRepository.undeliveredOrdersCoords()).thenReturn(result);
        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(300);
        when(orderRepository.undeliveredOrdersGroupThem(anyDouble(), anyDouble())).thenReturn(orderList);
        when(modelMapper.map(any(), any())).thenAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count == 0) {
                    count++;
                    return coord;
                }
                return ModelUtils.getOrderDto();
            }
        });
        GroupedOrderDto groupedOrderDto = ubsManagementService
            .getClusteredCoordsAlongWithSpecified(ModelUtils.getCoordinatesDtoSet(), 3000, 15).get(0);
        assertEquals(300, groupedOrderDto.getAmountOfLitres());
        assertEquals(groupedOrderDto.getGroupOfOrders().get(0), getOrderDto());
    }

    @Test
    void testAllUndeliveredOrdersWithLitersThrowException() {
        when(addressRepository.undeliveredOrdersCoords()).thenReturn(ModelUtils.getCoordinatesSet());

        List<Order> undeliveredOrders = new ArrayList<>();

        when(orderRepository.undeliveredAddresses()).thenReturn(undeliveredOrders);

        assertThrows(ActiveOrdersNotFoundException.class,
            () -> ubsManagementService.getAllUndeliveredOrdersWithLiters());
    }

    @Test
    void testGetAllUndeliveredOrdersWithLiters() {
        List<Order> allUndeliveredOrders = ModelUtils.getOrdersToGroupThem();

        when(addressRepository.undeliveredOrdersCoords()).thenReturn(ModelUtils.getCoordinatesSet());
        when(orderRepository.undeliveredAddresses()).thenReturn(allUndeliveredOrders);
        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(75, 25);

        for (Coordinates cord : ModelUtils.getCoordinatesSet()) {
            List<Order> currentOrders = allUndeliveredOrders.stream().filter(
                o -> o.getUbsUser().getAddress().getCoordinates().equals(cord)).collect(Collectors.toList());
            for (Order order : currentOrders) {
                when(modelMapper.map(order, OrderDto.class)).thenReturn(
                    OrderDto.builder().latitude(order.getUbsUser().getAddress().getCoordinates().getLatitude())
                        .longitude(order.getUbsUser().getAddress().getCoordinates().getLongitude()).build());
            }
        }

        List<GroupedOrderDto> expected = ubsManagementService.getAllUndeliveredOrdersWithLiters();
        List<GroupedOrderDto> actual = ModelUtils.getGroupedOrdersWithLiters();

        assertEquals(expected, actual);
    }

    @Test
    void testAddPointToUserThrowsException() {
        User user = ModelUtils.getTestUser();
        user.setUuid(null);

        AddingPointsToUserDto addingPointsToUserDto =
            AddingPointsToUserDto.builder().additionalPoints(anyInt()).build();
        assertThrows(UnexistingUuidExeption.class, () -> ubsManagementService.addPointsToUser(addingPointsToUserDto));
    }

    @Test
    void testAddPointsToUser() {
        User user = ModelUtils.getTestUser();
        user.setUuid(restClient.findUuidByEmail(user.getRecipientEmail()));
        user.setCurrentPoints(1);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ubsManagementService.addPointsToUser(AddingPointsToUserDto.builder().additionalPoints(anyInt()).build());

        assertEquals(2L, user.getChangeOfPointsList().size());
    }

    @ParameterizedTest
    @CsvSource({"1, Змінено менеджера обдзвону",
        "3, Змінено логіста",
        "4, Змінено штурмана",
        "5, Змінено водія"})
    void testUpdatePositionTest(long diffParam, String eventName) {
        User user = ModelUtils.getTestUser();
        user.setRecipientSurname("Gerasum");
        user.setRecipientName("Yuriy");
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER_UPDATE_POSITION));
        when(positionRepository.findById(2L)).thenReturn(Optional.ofNullable(TEST_POSITION));
        when(employeeRepository.findByName(anyString(), anyString())).thenReturn(Optional.ofNullable(TEST_EMPLOYEE));
        when(employeeOrderPositionRepository.saveAll(anyIterable()))
            .thenReturn(TEST_EMPLOYEE_ORDER_POSITION);
        when(employeeOrderPositionRepository.findPositionOfEmployeeAssignedForOrder(1L)).thenReturn(diffParam);
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(TEST_EMPLOYEE_ORDER_POSITION);
        ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE, "abc");
        verify(employeeOrderPositionRepository).findAllByOrderId(1L);
        verify(orderRepository).findById(1L);
        verify(positionRepository).findById(2L);
        verify(employeeRepository).findByName(anyString(), anyString());
        verify(employeeOrderPositionRepository).saveAll(anyIterable());
        Order order = TEST_ORDER_UPDATE_POSITION;
        verify(eventService, times(1)).save(eventName,
            order.getUser().getRecipientName() + "  " + order.getUser().getRecipientSurname(), order);
    }

    @Test
    void testUpdatePositionThrowsOrderNotFoundException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE, "abc"));
    }

    @Test
    void testUpdatePositionThrowsPositionNotFoundException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER_UPDATE_POSITION));
        when(positionRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(PositionNotFoundException.class,
            () -> ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE, "abc"));
    }

    @Test
    void testUpdatePositionThrowsEmployeeNotFoundException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER_UPDATE_POSITION));
        when(positionRepository.findById(2L)).thenReturn(Optional.of(TEST_POSITION));
        when(employeeRepository.findByName(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
            () -> ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE, "abc"));
    }

    @Test
    void testGetAdditionalBagsInfo() {
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.of(TEST_USER));
        when(additionalBagsInfoRepo.getAdditionalBagInfo(1L, TEST_USER.getRecipientEmail()))
            .thenReturn(TEST_MAP_ADDITIONAL_BAG_LIST);
        when(objectMapper.convertValue(any(), eq(AdditionalBagInfoDto.class)))
            .thenReturn(TEST_ADDITIONAL_BAG_INFO_DTO);

        List<AdditionalBagInfoDto> actual = ubsManagementService.getAdditionalBagsInfo(1L);

        assertEquals(TEST_ADDITIONAL_BAG_INFO_DTO_LIST, actual);

        verify(userRepository).findUserByOrderId(1L);
        verify(additionalBagsInfoRepo).getAdditionalBagInfo(1L, TEST_USER.getRecipientEmail());
        verify(objectMapper).convertValue(any(), eq(AdditionalBagInfoDto.class));
    }

    @Test
    void testGetAdditionalBagsInfoThrowsException() {
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.empty());

        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.getAdditionalBagsInfo(1L));
    }

    @Test
    void testSetOrderDetail() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusAdjustmentDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailConfirmed() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusConfirmedDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailFormed() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusFormedDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailNotTakenOut() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusNotTakenOutDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailOnTheRoute() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusOnThe_RouteDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailsDone() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderBroughtItHimself() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L))
            .thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusBROUGHT_IT_HIMSELFDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailsCanseled() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusCanseledDto()));
        when(languageRepository.findIdByCode("ua")).thenReturn(1L);
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "ua", "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void setOrderDetailExceptionTest() {
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));

        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.setOrderDetail(1L, confirm, exported, "ua", "abc"));
    }

    @Test
    void testSetOrderDetailThrowsUserNotFoundException() {
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();

        assertThrows(UserNotFoundException.class,
            () -> ubsManagementService.setOrderDetail(1L, confirm, exported, "ua", "abc"));
    }

    @Test
    void testAssignEmployeeWithThePositionToTheOrderException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        AssignEmployeesForOrderDto assignEmployeeForOrderDto = assignEmployeesForOrderDto();
        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto, "abc"));
    }

    @Test
    void testAssignEmployeeWithThePositionToTheOrderEmployeeAlreadyExistException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = getTestUser().getOrders().get(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(1L, 1L)).thenReturn(true);
        AssignEmployeesForOrderDto assignEmployeesForOrderDto = assignEmployeesForOrderDto();
        assertThrows(EmployeeAlreadyAssignedForOrder.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeesForOrderDto, "abc"));
    }

    @Test
    void testAssignEmployeesWithThePositionToTheOrderEmployeeNotFindException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = getTestUser().getOrders().get(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(1L, 1L)).thenReturn(false);
        AssignEmployeesForOrderDto assignEmployeeForOrderDto = assignEmployeesForOrderDto();
        assertThrows(EmployeeNotFoundException.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto, "abc"));
    }

    @Test
    void testAssignEmployeesToTheOrderEmployeeNotFindException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = getTestUser().getOrders().get(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(1L, 1L)).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        AssignEmployeesForOrderDto assignEmployeeForOrderDto = assignEmployeesForOrderDto();
        assertThrows(EmployeeNotFoundException.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto, "abc"));
    }

    @Test
    void testAssignEmployeesWithThePositionsNotFoundToEmployee() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = getTestUser().getOrders().get(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(1L, 1L)).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(employeeRepository.findPositionForEmployee(1L)).thenReturn(Optional.empty());
        AssignEmployeesForOrderDto assignEmployeeForOrderDto = assignEmployeesForOrderDto();
        assertThrows(PositionNotFoundException.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto, "abc"));
    }

    @Test
    void testAssignEmployeesWithThePositionsToTheOrderIsNotAssignedException() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = getTestUser().getOrders().get(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(1L, 1L)).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(employeeRepository.findPositionForEmployee(1L)).thenReturn(Optional.of(2L));
        AssignEmployeesForOrderDto assignEmployeeForOrderDto = assignEmployeesForOrderDto();
        assertThrows(EmployeeIsNotAssigned.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto, "abc"));
    }

    @ParameterizedTest
    @CsvSource({"1, Закріплено менеджера обдзвону",
        "3, Закріплено логіста",
        "4, Закріплено штурмана",
        "5, Закріплено водія"})
    void testAssignEmployeesWithThePositionsToTheOrderParams(long diffParam, String eventName) {
        User user = ModelUtils.getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        Order order = getTestUser().getOrders().get(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(1L, 1L)).thenReturn(false);
        Employee employee = getEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(employeeRepository.findPositionForEmployee(1L)).thenReturn(Optional.of(diffParam));
        ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto(), "abc");
        verify(employeeOrderPositionRepository, times(1)).save(any());
        verify(eventService, times(1)).save(eventName,
            employee.getFirstName() + "  " + employee.getLastName(), order);
    }

    @Test
    void testSaveAdminToOrder() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrder()));
        ubsManagementService.saveAdminCommentToOrder(ModelUtils.getAdminCommentDto(), "abc");
        verify(orderRepository, times(1)).save(ModelUtils.getOrder());
        verify(eventService, times(1)).save(any(), any(), any());
    }

    @Test
    void testUpdateEcoNumberForOrder() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrder()));
        ubsManagementService.updateEcoNumberForOrder(ModelUtils.getEcoNumberDto(), 1L, "abc");
        verify(eventService, times(1)).save(any(), any(), any());
    }

    @Test
    void testUpdateEcoNumberThrowOrderNotFoundException() {
        EcoNumberDto dto = ModelUtils.getEcoNumberDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(ModelUtils.getUser()));
        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.updateEcoNumberForOrder(dto, 1L, "uuid"));
    }

    @Test
    void checkAddUserViolationThrowsException() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setUser(user);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.ofNullable(order));
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(violationRepository.findByOrderId(order.getId())).thenReturn(Optional.of(ModelUtils.getViolation()));

        assertThrows(OrderViolationException.class,
            () -> ubsManagementService.addUserViolation(add, new MultipartFile[2], "abc"));
    }

    @Test
    void updateEcoNumberTrowsException() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.updateEcoNumberForOrder(ecoNumberDto, 1L, "abc"));
    }

    @Test
    void saveAdminCommentThrowsException() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        AdminCommentDto adminCommentDto = getAdminCommentDto();
        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.saveAdminCommentToOrder(adminCommentDto, "abc"));
    }

    @Test
    void getCertificatesWithFilter() {
        CertificateFilterCriteria certificateFilterCriteria = new CertificateFilterCriteria();
        CertificatePage certificatePage = new CertificatePage();
        List<Certificate> certificateList = Arrays.asList(new Certificate(), new Certificate());
        Pageable pageable = PageRequest.of(certificatePage.getPageNumber(), certificatePage.getPageSize());
        Page<Certificate> certificates = new PageImpl<>(certificateList, pageable, 1L);

        when(certificateCriteriaRepo.findAllWithFilter(certificatePage, certificateFilterCriteria))
            .thenReturn(certificates);

        ubsManagementService.getCertificatesWithFilter(certificatePage, certificateFilterCriteria);

        verify(certificateCriteriaRepo).findAllWithFilter(certificatePage, certificateFilterCriteria);
        assertEquals(certificateCriteriaRepo.findAllWithFilter(certificatePage, certificateFilterCriteria),
            certificates);
    }

    @Test
    void getOrdersForUserTest() {
        Order order1 = ModelUtils.getOrderUserFirst();
        Order order2 = ModelUtils.getOrderUserSecond();
        List<Order> orders = List.of(order1, order2);
        OrderInfoDto info1 = OrderInfoDto.builder().id(1l).orderPrice(10).build();
        OrderInfoDto info2 = OrderInfoDto.builder().id(2l).orderPrice(20).build();
        when(orderRepository.getAllOrdersOfUser("uuid")).thenReturn(orders);
        when(modelMapper.map(order1, OrderInfoDto.class)).thenReturn(info1);
        when(modelMapper.map(order2, OrderInfoDto.class)).thenReturn(info2);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(order1));
        when(orderRepository.getOrderDetails(2L)).thenReturn(Optional.ofNullable(order2));

        ubsManagementService.getOrdersForUser("uuid");

        verify(orderRepository).getAllOrdersOfUser("uuid");
        verify(modelMapper).map(order1, OrderInfoDto.class);
        verify(modelMapper).map(order2, OrderInfoDto.class);
        verify(orderRepository).getOrderDetails(1L);
        verify(orderRepository).getOrderDetails(2L);
    }

    @Test
    void getOrderStatusDataThrowsUnexistingOrderExceptionTest() {
        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.getOrderStatusData(100L, "ua");
        });
    }

    @Test
    void changeOrderTableView() {
        String uuid = "uuid1";
        CustomTableView customTableView = CustomTableView.builder()
            .titles("titles1,titles2")
            .uuid(uuid)
            .build();

        ubsManagementService.changeOrderTableView(uuid, "titles1,titles2");

        verify(customTableViewRepo).existsByUuid(uuid);
        verify(customTableViewRepo).save(customTableView);
    }

    @Test
    void changeOrderTableView2() {
        String uuid = "uuid1";

        when(customTableViewRepo.existsByUuid(uuid)).thenReturn(Boolean.TRUE);
        ubsManagementService.changeOrderTableView(uuid, "titles1,titles2");

        verify(customTableViewRepo).existsByUuid(uuid);
    }

    @Test
    void getCustomTableParameters() {
        String uuid = "uuid1";
        ubsManagementService.getCustomTableParameters(uuid);

        verify(customTableViewRepo).existsByUuid(uuid);
    }

    @Test
    void updateOrderAdminPageInfoTest() {
        OrderDetailStatusRequestDto orderDetailStatusRequestDto = ModelUtils.getTestOrderDetailStatusRequestDto();
        Order order = ModelUtils.getOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.paymentInfo(1L))
            .thenReturn(List.of(ModelUtils.getPayment()));
        when(userRepository.findUserByUuid("abc"))
            .thenReturn(Optional.of(ModelUtils.getUser()));
        lenient().when(ubsManagementServiceMock.updateOrderDetailStatus(1L, orderDetailStatusRequestDto, "abc"))
            .thenReturn(ModelUtils.getTestOrderDetailStatusDto());
        when(ubsClientService.updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(),
            "abc")).thenReturn(ModelUtils.getUbsCustomersDto());
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        when(addressRepository.findById(1L))
            .thenReturn(Optional.of(TEST_ADDRESS));
        when(receivingStationRepository.findAll())
            .thenReturn(List.of(ModelUtils.getReceivingStation()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "abc");

        verify(ubsClientService, times(1))
            .updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "abc");
    }

    @Test
    void updateOrderAdminPageInfoWithNullValuesTest() {
        UpdateOrderPageAdminDto emptyDto = new UpdateOrderPageAdminDto();
        ubsManagementServiceMock.updateOrderAdminPageInfo(emptyDto, 1L, "en", "abc");

        verify(ubsManagementServiceMock).updateOrderAdminPageInfo(emptyDto, 1L, "en", "abc");
    }

    @Test
    void updateOrderAdminPageInfoTestThrowsException() {
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(Order.builder().build()));
        assertThrows(UpdateAdminPageInfoException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "abc"));
    }

    @Test
    void getOrders() {
        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().orderDateFrom("ddd").build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(Page.empty());

        ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid");

        verify(bigOrderTableRepository).findAll(orderPage, orderSearchCriteria);
    }

    @Test
    void getOrdersCorrectCalculateWhenAllValueNotNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        OrderPage orderPage = OrderPage.builder()
            .pageNumber(1)
            .build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder()
            .search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateOrderStatusIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).getUbsUser().setFirstName(null);
        pageOrder.getContent().get(0).getUbsUser().setLastName(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setClientName("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculatePaymentIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setPayment(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setPaymentDate("-");
        bigOrderTableDTOPage.getContent().get(0).setTotalOrderSum(0L);
        bigOrderTableDTOPage.getContent().get(0).setPayment("-");
        bigOrderTableDTOPage.getContent().get(0).setAmountDue(-200L);

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateUbsUserIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setUbsUser(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setClientName("-");
        bigOrderTableDTOPage.getContent().get(0).setPhoneNumber("-");
        bigOrderTableDTOPage.getContent().get(0).setEmail("-");
        bigOrderTableDTOPage.getContent().get(0).setRegion("-");
        bigOrderTableDTOPage.getContent().get(0).setSettlement("-");
        bigOrderTableDTOPage.getContent().get(0).setDistrict("-");
        bigOrderTableDTOPage.getContent().get(0).setCommentToAddressForClient("-");
        bigOrderTableDTOPage.getContent().get(0).setAddress("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateAddressFieldsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).getUbsUser().getAddress().setRegion(null);
        pageOrder.getContent().get(0).getUbsUser().getAddress().setCity(null);
        pageOrder.getContent().get(0).getUbsUser().getAddress().setDistrict(null);
        pageOrder.getContent().get(0).getUbsUser().getAddress().setStreet(null);
        pageOrder.getContent().get(0).getUbsUser().getAddress().setHouseNumber(null);
        pageOrder.getContent().get(0).getUbsUser().getAddress().setHouseCorpus(null);
        pageOrder.getContent().get(0).getUbsUser().getAddress().setEntranceNumber(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setRegion("-");
        bigOrderTableDTOPage.getContent().get(0).setSettlement("-");
        bigOrderTableDTOPage.getContent().get(0).setDistrict("-");
        bigOrderTableDTOPage.getContent().get(0).setAddress("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateCertificatesNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setCertificates(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setOrderCertificateCode("-");
        bigOrderTableDTOPage.getContent().get(0).setOrderCertificatePoints("-");
        bigOrderTableDTOPage.getContent().get(0).setAmountDue(400L);

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateUserIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setUser(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setSenderName("-");
        bigOrderTableDTOPage.getContent().get(0).setSenderPhone("-");
        bigOrderTableDTOPage.getContent().get(0).setSenderEmail("-");
        bigOrderTableDTOPage.getContent().get(0).setViolationsAmount(0);

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateDateAndTimeOfExportNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setDeliverFrom(null);
        pageOrder.getContent().get(0).setDeliverTo(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setDateOfExport("-");
        bigOrderTableDTOPage.getContent().get(0).setTimeOfExport("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCalculateOrderDateNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setOrderDate(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setOrderDate("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectIdOrderFromShopDateNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setAdditionalOrders(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setIdOrderFromShop("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectEmployeeOrderPositionIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setEmployeeOrderPositions(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setResponsibleCaller("-");
        bigOrderTableDTOPage.getContent().get(0).setResponsibleDriver("-");
        bigOrderTableDTOPage.getContent().get(0).setResponsibleLogicMan("-");
        bigOrderTableDTOPage.getContent().get(0).setResponsibleNavigator("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectCommentsForOrderIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setNote(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setCommentsForOrder("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void getOrdersCorrectBlockedByEmployeeIsNull() {
        Page<Order> pageOrder = ModelUtils.getPageOrder();
        pageOrder.getContent().get(0).setBlockedByEmployee(null);

        Page<BigOrderTableDTO> bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        bigOrderTableDTOPage.getContent().get(0).setBlockedBy("-");

        OrderPage orderPage = OrderPage.builder().pageNumber(1).build();
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.builder().search("3333")
            .build();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageOrder);

        assertEquals(ubsManagementService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void updateUserViolation() {
        User user = ModelUtils.getUser();
        user.setUuid("uuid");
        UpdateViolationToUserDto updateViolationToUserDto = ModelUtils.getUpdateViolationToUserDto();
        Violation violation = ModelUtils.getViolation();

        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.ofNullable(user));
        when(violationRepository.findByOrderId(1L)).thenReturn(Optional.ofNullable(violation));

        ubsManagementService.updateUserViolation(updateViolationToUserDto, new MultipartFile[2], "uuid");

        assertEquals(2, violation.getImages().size());

        verify(userRepository).findUserByUuid("uuid");
        verify(violationRepository).findByOrderId(1L);
    }

    @Test
    void saveReason() {
        Order order = ModelUtils.getOrdersDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        ubsManagementService.saveReason(1L, "uu", Arrays.asList(new MultipartFile[2]));

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderSumDetailsForFormedOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getFormedOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForCanceledPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getCanceledPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForAdjustmentPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getAdjustmentPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForFormedHalfPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagByOrderId(1L)).thenReturn(TEST_BAG_LIST);

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForCanceledHalfPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getCanceledHalfPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagByOrderId(1L)).thenReturn(TEST_BAG_LIST);

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsThrowsUnexcitingOrderExceptionTest() {
        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.getOrderSumDetails(1L);
        });
    }

    @Test
    void getOrderStatusDataTest() {
        Order order = ModelUtils.getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = ModelUtils.getBagInfoDto();
        Language language = ModelUtils.getLanguage();

        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(order));
        when(bagRepository.findBagByOrderId(1L)).thenReturn(ModelUtils.getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(ModelUtils.getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findAll()).thenReturn(ModelUtils.getBag2list());
        when(languageRepository.findLanguageByCode("ua")).thenReturn(language);
        when(modelMapper.map(ModelUtils.getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(bagTranslationRepository.findNameByBagId(1, 1L)).thenReturn(new StringBuilder("name"));
        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(6, 0l))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L, 1L))
                .thenReturn("name");
        when(orderRepository.findById(6L)).thenReturn(Optional.ofNullable(order));
        when(receivingStationRepository.findAll()).thenReturn(ModelUtils.getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "ua");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagByOrderId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(bagRepository).findAll();
        verify(languageRepository).findLanguageByCode("ua");
        verify(modelMapper).map(ModelUtils.getBaglist().get(0), BagInfoDto.class);
        verify(bagTranslationRepository).findNameByBagId(1, 1L);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationByIdAndLanguageId(6, 0L);
        verify(orderPaymentStatusTranslationRepository).findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L,
            1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void getOrderStatusDataTestEmptyPriceDetails() {
        Order order = ModelUtils.getOrderForGetOrderStatusEmptyPriceDetails();
        BagInfoDto bagInfoDto = ModelUtils.getBagInfoDto();
        Language language = ModelUtils.getLanguage();

        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findAll()).thenReturn(ModelUtils.getBag2list());
        when(languageRepository.findLanguageByCode("ua")).thenReturn(language);
        when(modelMapper.map(ModelUtils.getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(bagTranslationRepository.findNameByBagId(1, 1L)).thenReturn(new StringBuilder("name"));
        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(6, 0l))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L, 1L))
                .thenReturn("name");
        when(orderRepository.findById(6L)).thenReturn(Optional.ofNullable(order));
        when(receivingStationRepository.findAll()).thenReturn(ModelUtils.getReceivingList());
        when(modelMapper.map(getOrderForGetOrderStatusData2Test().getPayment().get(0), PaymentInfoDto.class))
            .thenReturn(ModelUtils.getInfoPayment());

        ubsManagementService.getOrderStatusData(1L, "ua");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagByOrderId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(bagRepository).findAll();
        verify(languageRepository).findLanguageByCode("ua");
        verify(modelMapper).map(ModelUtils.getBaglist().get(0), BagInfoDto.class);
        verify(bagTranslationRepository).findNameByBagId(1, 1L);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationByIdAndLanguageId(6, 0L);
        verify(orderPaymentStatusTranslationRepository).findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L,
            1L);
        verify(receivingStationRepository).findAll();

    }

    @Test
    void getOrderStatusDataWithEmptyCertificateTest() {
        Order order = ModelUtils.getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = ModelUtils.getBagInfoDto();
        Language language = ModelUtils.getLanguage();

        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(order));
        when(bagRepository.findBagByOrderId(1L)).thenReturn(ModelUtils.getBaglist());

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findAll()).thenReturn(ModelUtils.getBag2list());
        when(languageRepository.findLanguageByCode("ua")).thenReturn(language);
        when(modelMapper.map(ModelUtils.getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(bagTranslationRepository.findNameByBagId(1, 1L)).thenReturn(new StringBuilder("name"));
        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(6, 0l))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L, 1L))
                .thenReturn("name");
        when(orderRepository.findById(6L)).thenReturn(Optional.ofNullable(order));
        when(receivingStationRepository.findAll()).thenReturn(ModelUtils.getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "ua");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagByOrderId(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(bagRepository).findAll();
        verify(languageRepository).findLanguageByCode("ua");
        verify(modelMapper).map(ModelUtils.getBaglist().get(0), BagInfoDto.class);
        verify(bagTranslationRepository).findNameByBagId(1, 1L);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationByIdAndLanguageId(6, 0L);
        verify(orderPaymentStatusTranslationRepository).findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L,
            1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void getOrderStatusDataExceptionTest() {
        Order order = ModelUtils.getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = ModelUtils.getBagInfoDto();
        Language language = ModelUtils.getLanguage();

        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(order));
        when(bagRepository.findBagByOrderId(1L)).thenReturn(ModelUtils.getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(ModelUtils.getCertificateList());

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findAll()).thenReturn(ModelUtils.getBag2list());
        when(languageRepository.findLanguageByCode("ua")).thenReturn(language);
        when(modelMapper.map(ModelUtils.getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(bagTranslationRepository.findNameByBagId(1, 1L)).thenReturn(new StringBuilder("name"));
        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(6, 0l))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L, 1L))
                .thenReturn("name");
        assertThrows(ReceivingStationNotFoundException.class, () -> {
            ubsManagementService.getOrderStatusData(1L, "ua");
        });

    }
}
