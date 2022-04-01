package greencity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.SortingOrder;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.*;
import greencity.service.ubs.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UBSManagementServiceImplTest {
    @Mock(lenient = true)
    AddressRepository addressRepository;

    @Mock
    private FileService fileService;

    @Mock(lenient = true)
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

    @Mock(lenient = true)
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
    OrdersAdminsPageService ordersAdminsPageService;

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
    void getAddressByOrderId() {
        Order order = ModelUtils.getOrder();
        ReadAddressByOrderDto readAddressByOrderDto = ModelUtils.getReadAddressByOrderDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(ubsManagementService.getAddressByOrderId(1L)).thenReturn(readAddressByOrderDto);
        Assertions.assertNotNull(order);
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

        List<ReceivingStation> stations =
            Arrays.asList(new ReceivingStation().setName("a"), new ReceivingStation().setName("b"));
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
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));

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
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
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
        assertEquals(100L, ubsManagementService.getPaymentInfo(order.getId(), 800L).getOverpayment());
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
        OrderDetailStatusRequestDto requestDto = getTestOrderDetailStatusRequestDto();
        assertThrows(PaymentNotFoundException.class, () -> {
            ubsManagementService.updateOrderDetailStatus(1L, requestDto, "uuid");
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
        assertEquals(0, order.getPointsToUse());

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
        when(bagTranslationRepository.findAllByOrder(1L)).thenReturn(TEST_BAG_TRANSLATION_LIST);
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
        verify(bagTranslationRepository).findAllByOrder(1L);
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
    @CsvSource({"2, Змінено менеджера обдзвону",
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
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailConfirmed() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusConfirmedDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailFormed() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusFormedDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailNotTakenOut() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusNotTakenOutDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailOnTheRoute() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusOnThe_RouteDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailsDone() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

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
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailsCanseled() {
        User user = User.builder().uuid("abc").recipientName("Петро").recipientSurname("Петренко")
            .id(42L).build();
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusCanseledDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

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
            () -> ubsManagementService.setOrderDetail(1L, confirm, exported, "abc"));
    }

    @Test
    void testSetOrderDetailThrowsUserNotFoundException() {
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();

        assertThrows(UserNotFoundException.class,
            () -> ubsManagementService.setOrderDetail(1L, confirm, exported, "abc"));
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
        when(employeeRepository.findPositionForEmployee(1L)).thenReturn(Optional.of(1L));
        AssignEmployeesForOrderDto assignEmployeeForOrderDto = assignEmployeesForOrderDto();
        assertThrows(EmployeeIsNotAssigned.class,
            () -> ubsManagementService.assignEmployeesWithThePositionsToTheOrder(assignEmployeeForOrderDto, "abc"));
    }

    @ParameterizedTest
    @CsvSource({"2, Закріплено менеджера обдзвону",
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
    void updateEcoNumberTrowsException() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.updateEcoNumberForOrder(ecoNumberDto, 1L, "abc"));
    }

    @Test
    void updateEcoNumberTrowsIncorrectEcoNumberFormatException() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrder()));

        EcoNumberDto ecoNumberDto = ModelUtils.getEcoNumberDto();
        ecoNumberDto.setEcoNumber(new HashSet<>(Arrays.asList("1234a")));
        assertThrows(IncorrectEcoNumberFormatException.class,
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
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "abc");
        UpdateOrderPageAdminDto emptyDto = new UpdateOrderPageAdminDto();
        ubsManagementService.updateOrderAdminPageInfo(emptyDto, 1L, "en", "abc");

        verify(ubsClientService, times(1))
            .updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "abc");
    }

    @Test
    void updateOrderAdminPageInfoTestThrowsException() {
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(Order.builder().build()));
        assertThrows(UpdateAdminPageInfoException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "abc"));
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
        when(bagRepository.findBagByOrderId(1L)).thenReturn(getBaglist());

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
        when(bagRepository.findBagByOrderId(1L)).thenReturn(getBaglist());

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
        when(bagTranslationRepository.findNameByBagId(1)).thenReturn(new StringBuilder("name"));
        when(bagTranslationRepository.findNameEngByBagId(1)).thenReturn(new StringBuilder("name"));
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
        verify(bagTranslationRepository).findNameByBagId(1);
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
        when(bagTranslationRepository.findNameByBagId(1)).thenReturn(new StringBuilder("name"));
        when(bagTranslationRepository.findNameEngByBagId(1)).thenReturn(new StringBuilder("name"));
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
        verify(bagTranslationRepository).findNameByBagId(1);
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
        when(bagTranslationRepository.findNameByBagId(1)).thenReturn(new StringBuilder("name"));
        when(bagTranslationRepository.findNameEngByBagId(1)).thenReturn(new StringBuilder("name"));
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
        verify(bagTranslationRepository).findNameByBagId(1);
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
        when(bagTranslationRepository.findNameByBagId(1)).thenReturn(new StringBuilder("name"));
        when(bagTranslationRepository.findNameEngByBagId(1)).thenReturn(new StringBuilder("name"));
        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(6, 0l))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L, 1L))
                .thenReturn("name");
        assertThrows(ReceivingStationNotFoundException.class, () -> {
            ubsManagementService.getOrderStatusData(1L, "ua");
        });

    }

    @Test
    void updateOrderExportDetails() {
        User user = getTestUser();
        Order order = getOrder();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(user.getId(), testDetails, user.getUuid());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderExportDetailsEmptyDetailsTest() {
        User user = getTestUser();
        Order order = getOrder();
        order.setDeliverFrom(null);
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate emptyDetails = ExportDetailsDtoUpdate.builder().receivingStationId(1L).build();
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(user.getId(), emptyDetails, user.getUuid());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderExportDetailsUserNotFoundExceptionTest() {
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetails(1L, testDetails, "abc"));
    }

    @Test
    void updateOrderExportDetailsUnexistingOrderExceptionTest() {
        User user = getTestUser();
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.updateOrderExportDetails(1L, testDetails, "abc"));
    }

    @Test
    void updateOrderExportDetailsReceivingStationNotFoundExceptionTest() {
        User user = getTestUser();
        Order order = getOrder();
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = ModelUtils.getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(ReceivingStationNotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetails(1L, testDetails, "abc"));
    }

    @Test
    void getPaymentInfo() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        PaymentInfoDto paymentInfo = getInfoPayment();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(modelMapper.map(any(), eq(PaymentInfoDto.class))).thenReturn(paymentInfo);

        ubsManagementService.getPaymentInfo(order.getId(), 100L);
        verify(notificationService, times(1)).notifyBonuses(any(), any());
    }

    @Test
    void getPaymentInfoExceptionTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.getPaymentInfo(1L, 100L));
    }

    @Test
    void updateManualPayment() {
        User user = getUser();
        Order order = getOrderUserFirst();
        Payment payment = getPayment();
        ManualPaymentRequestDto requestDto = getManualPaymentRequestDto();
        requestDto.setImagePath("");
        payment.setImagePath("abc");
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));

        ubsManagementService.updateManualPayment(payment.getId(), requestDto, file, user.getUuid());

        verify(paymentRepository).save(any(Payment.class));
        verify(eventService).save(any(), any(), any());
    }

    @Test
    void updateManualPaymentUserNotFoundExceptionTest() {
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1L, null, null, "abc"));
    }

    @Test
    void updateManualPaymentPaymentNotFoundExceptionTest() {
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(getUser()));
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1L, null, null, "abc"));
    }

    @Test
    void updateAllOrderAdminPageInfoUnexistingOrderExceptionTest() {
        Order order = ModelUtils.getOrder();
        UpdateAllOrderPageDto updateAllOrderPageDto = ModelUtils.updateAllOrderPageDto(OrderStatus.CONFIRMED);
        when(orderRepository.findById(4L)).thenReturn(Optional.ofNullable(order));
        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua"));
    }

    @Test
    void updateAllOrderAdminPageInfoUpdateAdminPageInfoExceptionTest() {
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(Order.builder().build()));
        assertThrows(UpdateAdminPageInfoException.class,
            () -> ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua"));
    }

    @Test
    void updateAllOrderAdminPageInfoStatusConfirmedTest() {
        Order order = ModelUtils.getOrder();
        order.setOrderDate(LocalDateTime.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        UpdateAllOrderPageDto expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.CONFIRMED);
        UpdateAllOrderPageDto actual = ModelUtils.updateAllOrderPageDto(OrderStatus.CONFIRMED);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.ADJUSTMENT);
        actual = ModelUtils.updateAllOrderPageDto(OrderStatus.ADJUSTMENT);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.DONE);
        actual = ModelUtils.updateAllOrderPageDto(OrderStatus.DONE);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.BROUGHT_IT_HIMSELF);
        actual = ModelUtils.updateAllOrderPageDto(OrderStatus.BROUGHT_IT_HIMSELF);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.ON_THE_ROUTE);
        actual = ModelUtils.updateAllOrderPageDto(OrderStatus.ON_THE_ROUTE);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.NOT_TAKEN_OUT);
        actual = ModelUtils.updateAllOrderPageDto(OrderStatus.NOT_TAKEN_OUT);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = ModelUtils.updateAllOrderPageDto(OrderStatus.CANCELED);
        actual = ModelUtils.updateAllOrderPageDto(OrderStatus.CANCELED);
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");
    }

    @Test
    void updateAllOrderAdminPageInfoAdditionalOrdersEmptyTest() {
        Order order = ModelUtils.getOrder2();
        UpdateAllOrderPageDto updateAllOrderPageDto = ModelUtils.updateAllOrderPageDto(OrderStatus.CANCELED);
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua");

        verify(orderRepository).findById(1L);
    }

    @Test
    void checkGetPaymentInfoWhenPaymentsWithCertificatesAndPointsSmallerThanSumToPay() {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(0L, ubsManagementService.getPaymentInfo(order.getId(), 1100L).getOverpayment());
    }

    @Test
    void testAddPointsToUserWhenCurrentPointIsNull() {
        User user = ModelUtils.getTestUser();
        user.setUuid(restClient.findUuidByEmail(user.getRecipientEmail()));
        user.setCurrentPoints(null);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ubsManagementService.addPointsToUser(AddingPointsToUserDto.builder().additionalPoints(anyInt()).build());

        assertEquals(2L, user.getChangeOfPointsList().size());
    }

    @Test
    void saveReasonWhenListElementsAreNotNulls() {
        Order order = ModelUtils.getOrdersDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        ubsManagementService.saveReason(1L, "uu", Arrays.asList(new MultipartFile[] {
            new MockMultipartFile("Name", new byte[2]), new MockMultipartFile("Name", new byte[2])}));

        verify(orderRepository).findById(1L);
    }

    @Test
    void saveNewManualPaymentWhenImageNotNull() {
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
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, Mockito.mock(MultipartFile.class), "abc");

        verify(eventService, times(1))
            .save("Замовлення Оплачено", "Система", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1l);
    }

    @Test
    void getOrderStatusDataWithNotEmptyLists() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        Language language = getLanguage();
        OrderStatusTranslation orderStatusTranslation = mock(OrderStatusTranslation.class);
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = mock(OrderPaymentStatusTranslation.class);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(order));
        when(bagRepository.findBagByOrderId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findAll()).thenReturn(getBag2list());
        when(languageRepository.findLanguageByCode("ua")).thenReturn(language);
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(bagTranslationRepository.findNameByBagId(1)).thenReturn(new StringBuilder("name"));
        when(bagTranslationRepository.findNameEngByBagId(1)).thenReturn(new StringBuilder("name"));
        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(6, 0l))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(1L,
                1L))
                    .thenReturn("name");
        when(
            orderStatusTranslationRepository.getOrderStatusTranslationsByLanguageId(language.getId()))
                .thenReturn(List.of(orderStatusTranslation));
        when(
            orderPaymentStatusTranslationRepository.getOrderStatusPaymentTranslationsByLanguageId(language.getId()))
                .thenReturn(List.of(orderPaymentStatusTranslation));

        when(orderRepository.findById(6L)).thenReturn(Optional.ofNullable(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "ua");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagByOrderId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(bagRepository).findAll();
        verify(languageRepository).findLanguageByCode("ua");
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(bagTranslationRepository).findNameByBagId(1);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationByIdAndLanguageId(6, 0L);
        verify(orderPaymentStatusTranslationRepository).findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(
            1L,
            1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void deleteManualPaymentTest() {
        Payment payment = ModelUtils.getManualPayment();
        User user = ModelUtils.getTestUser();
        Order order = getFormedOrder();

        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        ubsManagementService.deleteManualPayment(1L, "abc");

        verify(userRepository).findUserByUuid("abc");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deletePaymentById(1L);
        verify(fileService).delete(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + 1L,
            user.getRecipientName() + "  " + user.getRecipientSurname(), payment.getOrder());

    }

    @Test
    void deleteManualTestWithoutImage() {
        Payment payment = ModelUtils.getManualPayment();
        User user = ModelUtils.getTestUser();
        Order order = getFormedOrder();
        payment.setImagePath(null);

        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        ubsManagementService.deleteManualPayment(1L, "abc");

        verify(userRepository).findUserByUuid("abc");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deletePaymentById(1L);
        verify(fileService, times(0)).delete(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + 1L,
            user.getRecipientName() + "  " + user.getRecipientSurname(), payment.getOrder());
    }

    @Test
    void deleteManualTestWithoutUser() {
        when(userRepository.findUserByUuid("uuid25")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            ubsManagementService.deleteManualPayment(1L, "uuid25");
        });
    }

    @Test
    void deleteManualTestWithoutPayment() {
        User user = getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(paymentRepository.findById(25L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> {
            ubsManagementService.deleteManualPayment(25L, "abc");
        });
    }

    @Test
    void addBonusesToUserTest() {
        User user = getTestUser();
        Order order = getOrder2();
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ubsManagementService.addBonusesToUser(ModelUtils.getAddBonusesToUserDto(), 1L);

        verify(userRepository).findUserByOrderId(1L);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(userRepository).save(user);
    }

    @Test
    void addBonusesToUserWithoutUserTest() {
        when(userRepository.findUserByOrderId(20L)).thenReturn(Optional.empty());
        AddBonusesToUserDto dto = getAddBonusesToUserDto();
        assertThrows(UserNotFoundException.class, () -> {
            ubsManagementService.addBonusesToUser(dto, 20L);
        });
    }
}
