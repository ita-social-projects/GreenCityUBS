package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.OrderHistory;
import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.bag.ReasonNotTakeBagDto;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.NotTakenOrderReasonDto;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.Refund;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.SortingOrder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.EmployeeOrderPositionRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderDetailRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.RefundRepository;
import greencity.repository.ServiceRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.locations.LocationApiService;
import greencity.service.notification.NotificationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static greencity.ModelUtils.*;
import static greencity.constant.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UBSManagementServiceImplTest {
    @Mock(lenient = true)
    OrderAddressRepository orderAddressRepository;
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
    @Mock
    private OrderBagRepository orderBagRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentUtil paymentUtil;

    @Test
    void getAllCertificates() {
        Pageable pageable =
            PageRequest.of(0, 5, Sort.by(Sort.Direction.fromString(SortingOrder.DESC.toString()), "points"));
        CertificateDtoForSearching certificateDtoForSearching = ModelUtils.getCertificateDtoForSearching();
        List<Certificate> certificates =
            Collections.singletonList(ModelUtils.getActiveCertificateWith10Points());
        List<CertificateDtoForSearching> certificateDtoForSearchings =
            Collections.singletonList(certificateDtoForSearching);
        PageableDto<CertificateDtoForSearching> certificateDtoForSearchingPageableDto =
            new PageableDto<>(certificateDtoForSearchings, certificateDtoForSearchings.size(), 0, 1);
        Page<Certificate> certificates1 = new PageImpl<>(certificates, pageable, certificates.size());
        when(modelMapper.map(certificates.getFirst(), CertificateDtoForSearching.class))
            .thenReturn(certificateDtoForSearching);
        when(certificateRepository.findAll(pageable)).thenReturn(certificates1);
        PageableDto<CertificateDtoForSearching> actual =
            ubsManagementService.getAllCertificates(pageable, "points", SortingOrder.DESC);
        assertEquals(certificateDtoForSearchingPageableDto, actual);
    }

    @Test
    void checkOrderNotFound() {
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getAddressByOrderId(10000000L));
    }

    @Test
    void getAddressByOrderId() {
        Order order = getOrder();
        ReadAddressByOrderDto readAddressByOrderDto = ModelUtils.getReadAddressByOrderDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(ubsManagementService.getAddressByOrderId(1L)).thenReturn(readAddressByOrderDto);
        Assertions.assertNotNull(order);
    }

    @Test
    void checkPaymentNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderDetailStatus(100L));
    }

    @Test
    void returnExportDetailsByOrderId() {
        ExportDetailsDto expected = ModelUtils.getExportDetails();
        Order order = getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<ReceivingStation> stations =
            Arrays.asList(ReceivingStation.builder().name("a").build(), ReceivingStation.builder().name("b").build());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        assertEquals(expected, ubsManagementService.getOrderExportDetails(1L));
    }

    @Test
    void updateExportDetailsByOrderId() {
        ExportDetailsDtoUpdate dto = getExportDetailsRequest();
        Order order = getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));

        List<ReceivingStation> stations = List.of(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetailsById(order.getId(), dto, "abc");

        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void updateExportDetailsNotSuccessfulByOrderId() {
        ExportDetailsDtoUpdate dto = getExportDetailsRequest();
        Order order = getOrderExportDetailsWithNullValues();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        List<ReceivingStation> stations = List.of(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetailsById(order.getId(), dto, "abc");

        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void checkStationNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderExportDetails(100L));
    }

    @Test
    void updateOrderDetailStatusThrowException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrder()));
        OrderDetailStatusRequestDto requestDto = getTestOrderDetailStatusRequestDto();
        assertThrows(NotFoundException.class,
                () -> ubsManagementService.updateOrderDetailStatusById(1L, requestDto, "uuid"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderDetailStatusFirst() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = user.getOrders().getFirst();
        order.setOrderDate((LocalDateTime.of(2021, 5, 15, 10, 20, 5)));

        List<Payment> payment = new ArrayList<>();
        payment.add(Payment.builder().build());

        order.setPayment(payment);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(payment);
        when(paymentRepository.saveAll(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenReturn(order);

        OrderDetailStatusRequestDto testOrderDetail = getTestOrderDetailStatusRequestDto();
        OrderDetailStatusDto expectedObject = ModelUtils.getTestOrderDetailStatusDto();
        OrderDetailStatusDto producedObject = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");
        assertEquals(expectedObject.getOrderStatus(), producedObject.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObject.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObject.getDate());
        testOrderDetail.setOrderStatus(OrderStatus.FORMED.toString());
        expectedObject.setOrderStatus(OrderStatus.FORMED.toString());
        OrderDetailStatusDto producedObjectAdjustment = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectAdjustment.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectAdjustment.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectAdjustment.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.ADJUSTMENT.toString());
        expectedObject.setOrderStatus(OrderStatus.ADJUSTMENT.toString());
        OrderDetailStatusDto producedObjectConfirmed = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectConfirmed.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectConfirmed.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectConfirmed.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.CONFIRMED.toString());
        expectedObject.setOrderStatus(OrderStatus.CONFIRMED.toString());
        OrderDetailStatusDto producedObjectNotTakenOut = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectNotTakenOut.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectNotTakenOut.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectNotTakenOut.getDate());

        testOrderDetail.setCancellationReason("MOVING_OUT");
        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled.getDate());
        assertEquals(700, order.getPointsToUse());

        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        testOrderDetail.setOrderStatus(OrderStatus.DONE.toString());
        expectedObject.setOrderStatus(OrderStatus.DONE.toString());
        OrderDetailStatusDto producedObjectDone = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectDone.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectDone.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectDone.getDate());

        order.setPointsToUse(0);
        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled2 = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled2.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled2.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled2.getDate());
        assertEquals(0, order.getPointsToUse());

        order.setOrderStatus(OrderStatus.ADJUSTMENT);
        testOrderDetail.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF.toString());
        expectedObject.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF.toString());
        OrderDetailStatusDto producedObjectBroughtItHimself = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectBroughtItHimself.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectBroughtItHimself.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectBroughtItHimself.getDate());

        order.setCertificates(Set.of(ModelUtils.getActiveCertificateWith10Points()));
        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled3 = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled3.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled3.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled3.getDate());
        assertEquals(0, order.getPointsToUse());

        verify(eventService, times(1))
            .saveEvent("Статус Замовлення - Узгодження",
                "test@gmail.com", order);
        verify(eventService, times(1))
            .saveEvent("Статус Замовлення - Підтверджено",
                "test@gmail.com", order);
        verify(eventService, times(3))
            .saveEvent("Статус Замовлення - Скасовано",
                "test@gmail.com", order);
        verify(eventService, times(1))
            .saveEvent("Невикористані бонуси повернено на бонусний рахунок клієнта" + ". Всього " + 700,
                "test@gmail.com", order);
    }

    @Test
    void updateOrderDetailStatusSecond() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = user.getOrders().getFirst();
        order.setOrderDate((LocalDateTime.now()));

        List<Payment> payment = new ArrayList<>();
        payment.add(Payment.builder().build());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(payment);
        when(paymentRepository.saveAll(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenReturn(order);

        OrderDetailStatusRequestDto testOrderDetail = getTestOrderDetailStatusRequestDto();
        OrderDetailStatusDto expectedObject = ModelUtils.getTestOrderDetailStatusDto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        expectedObject.setDate(LocalDateTime.now().format(formatter));
        testOrderDetail.setOrderStatus(OrderStatus.ON_THE_ROUTE.toString());
        expectedObject.setOrderStatus(OrderStatus.ON_THE_ROUTE.toString());
        OrderDetailStatusDto producedObjectOnTheRoute = ubsManagementService
            .updateOrderDetailStatusById(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectOnTheRoute.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectOnTheRoute.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectOnTheRoute.getDate());

        verify(orderRepository).findById(1L);
        verify(paymentRepository).findAllByOrderId(1L);
        verify(paymentRepository).saveAll(any());
        verify(orderRepository, times(2)).save(any());
    }

    @Test
    void updateOrderDetailStatusToBroughtItHimselfTest() {
        String email = "some@email.com";
        Order saved = getOrder();
        saved.setOrderStatus(OrderStatus.FORMED);
        Order updated = getOrder();
        updated.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        when(paymentRepository.findAllByOrderId(saved.getId())).thenReturn(saved.getPayment());

        OrderDetailStatusRequestDto detailStatusDto = OrderDetailStatusRequestDto.builder()
            .orderStatus("BROUGHT_IT_HIMSELF")
            .build();

        OrderDetailStatusDto result = ubsManagementService.updateOrderDetailStatus(saved, detailStatusDto, email);

        verify(eventService).saveEvent(OrderHistory.ORDER_BROUGHT_IT_HIMSELF, email, updated);
        verify(notificationService).notifySelfPickupOrder(updated);
        verify(orderRepository).save(updated);

        assertEquals(OrderStatus.BROUGHT_IT_HIMSELF.name(), result.getOrderStatus());
    }

    @Test
    void getAllEmployeesByPosition() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        EmployeePositionDtoRequest dto = ModelUtils.getEmployeePositionDtoRequest();
        List<EmployeeOrderPosition> newList = new ArrayList<>();
        newList.add(ModelUtils.getEmployeeOrderPosition());
        List<Position> positionList = new ArrayList<>();
        positionList.add(ModelUtils.getPosition());
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(getEmployee());
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(employeeOrderPositionRepository.findAllByOrderId(anyLong())).thenReturn(newList);
        when(positionRepository.findAll()).thenReturn(positionList);
        when(employeeRepository.findAllByEmployeePositionId(anyLong())).thenReturn(employeeList);
        assertEquals(dto, ubsManagementService.getAllEmployeesByPosition(1L, "test@gmail.com"));
        verify(orderRepository).findById(anyLong());
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(employeeOrderPositionRepository).findAllByOrderId(anyLong());
        verify(positionRepository).findAll();
        verify(employeeRepository).findAllByEmployeePositionId(anyLong());
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getAllEmployeesByPositionThrowBadRequestException() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.getAllEmployeesByPosition(1L, "test@gmail.com"));
        verify(orderRepository).findById(anyLong());
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void testUpdateAddress() {
        Order order = getOrder();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setId(1L);
        OrderAddressExportDetailsDtoUpdate dtoUpdate = ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        when(orderAddressRepository.findById(dtoUpdate.getAddressId())).thenReturn(Optional.of(orderAddress));

        when(orderAddressRepository.save(orderAddress)).thenReturn(orderAddress);
        when(modelMapper.map(orderAddress, OrderAddressDtoResponse.class)).thenReturn(TEST_ORDER_ADDRESS_DTO_RESPONSE);
        Optional<OrderAddressDtoResponse> actual =
            ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, order, "test@gmail.com");
        assertEquals(Optional.of(TEST_ORDER_ADDRESS_DTO_RESPONSE), actual);

        verify(orderAddressRepository).findById(dtoUpdate.getAddressId());
        verify(orderAddressRepository).save(orderAddress);
        verify(modelMapper).map(orderAddress, OrderAddressDtoResponse.class);
    }

    @Test
    void testUpdateAddressThrowsOrderNotFoundException() {
        Order order = getOrder();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, order, "abc"));
    }

    @Test
    void testUpdateAddressThrowsNotFoundOrderAddressException() {
        Order order = getOrder();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, order, "abc"));
    }

    @Test
    void testGetOrderDetailStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(TEST_PAYMENT_LIST);

        OrderDetailStatusDto actual = ubsManagementService.getOrderDetailStatus(1L);

        assertEquals(ORDER_DETAIL_STATUS_DTO.getOrderStatus(), actual.getOrderStatus());
        assertEquals(ORDER_DETAIL_STATUS_DTO.getPaymentStatus(), actual.getPaymentStatus());
        assertEquals(ORDER_DETAIL_STATUS_DTO.getDate(), actual.getDate());

        verify(orderRepository).findById(1L);
        verify(paymentRepository).findAllByOrderId(1L);
    }

    @Test
    void testGetOrderDetailStatusThrowsUnExistingOrderException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ubsManagementService.getOrderDetailStatus(1L));
    }

    @Test
    void testGetOrderDetailStatusThrowsPaymentNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(paymentRepository.findAllByOrderId(1)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> ubsManagementService.getOrderDetailStatus(1L));
    }

    @Test
    void testGetOrderDetails() {
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(TEST_ORDER));
        when(modelMapper.map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType())).thenReturn(TEST_BAG_MAPPING_DTO_LIST);
        when(orderBagService.findAllBagsByOrderId(1L)).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(TEST_BAG, BagInfoDto.class)).thenReturn(TEST_BAG_INFO_DTO);
        when(bagRepository.findAllByOrder(1L)).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()))).thenReturn(TEST_ORDER_DETAILS_INFO_DTO_LIST);

        List<OrderDetailInfoDto> actual = ubsManagementService.getOrderDetails(1L, "ua");

        assertEquals(TEST_ORDER_DETAILS_INFO_DTO_LIST, actual);

        verify(orderRepository).getOrderDetails(1L);
        verify(modelMapper).map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType());
        verify(orderBagService).findAllBagsByOrderId(1L);
        verify(bagRepository, times(1)).findAllByOrder(anyLong());
        verify(modelMapper).map(TEST_BAG, BagInfoDto.class);
        verify(modelMapper).map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()));
    }

    @Test
    void testGetOrdersBagsDetails() {
        List<DetailsOrderInfoDto> detailsOrderInfoDtoList = new ArrayList<>();
        Map<String, Object> mapOne = Map.of("One", "Two");
        Map<String, Object> mapTwo = Map.of("One", "Two");
        List<Map<String, Object>> mockBagInfoRepository = Arrays.asList(mapOne, mapTwo);
        when(bagRepository.getBagInfo(1L)).thenReturn(mockBagInfoRepository);
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
                .thenReturn(Optional.of(getOrder()));
        List<ReceivingStation> receivingStations = new ArrayList<>();
        when(receivingStationRepository.findAll())
                .thenReturn(receivingStations);
        assertThrows(NotFoundException.class,
                () -> ubsManagementService.getOrderExportDetails(1L));
    }

    @Test
    void testGetOrderDetailsThrowsException() {
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ubsManagementService.getOrderDetails(1L, "ua"));
    }

    @Test
    void checkGetAllUserViolations() {
        User user = ModelUtils.getUser();
        user.setUuid(userRemoteClient.findUuidByEmail(user.getRecipientEmail()));
        user.setViolations(1);

        ViolationsInfoDto expected = modelMapper.map(user, ViolationsInfoDto.class);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));

        ViolationsInfoDto actual = ubsManagementService.getAllUserViolations(user.getRecipientEmail());

        assertEquals(expected, actual);
    }

    @Test
    void testAddPointToUserThrowsException() {
        User user = getTestUser();
        user.setUuid(null);

        AddingPointsToUserDto addingPointsToUserDto =
            AddingPointsToUserDto.builder().additionalPoints(anyInt()).build();
        assertThrows(NotFoundException.class, () -> ubsManagementService.addPointsToUser(addingPointsToUserDto));
    }

    @Test
    void testAddPointsToUser() {
        User user = getTestUser();
        user.setUuid(userRemoteClient.findUuidByEmail(user.getRecipientEmail()));
        user.setCurrentPoints(1);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ubsManagementService.addPointsToUser(AddingPointsToUserDto.builder().additionalPoints(anyInt()).build());

        assertEquals(2L, user.getChangeOfPointsList().size());
    }

    @Test
    void testGetAdditionalBagsInfo() {
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.of(TEST_USER));
        when(bagRepository.getAdditionalBagInfo(1L, TEST_USER.getRecipientEmail()))
                .thenReturn(TEST_MAP_ADDITIONAL_BAG_LIST);
        when(objectMapper.convertValue(any(), eq(AdditionalBagInfoDto.class)))
                .thenReturn(TEST_ADDITIONAL_BAG_INFO_DTO);

        List<AdditionalBagInfoDto> actual = ubsManagementService.getAdditionalBagsInfo(1L);

        assertEquals(TEST_ADDITIONAL_BAG_INFO_DTO_LIST, actual);

        verify(userRepository).findUserByOrderId(1L);
        verify(bagRepository).getAdditionalBagInfo(1L, TEST_USER.getRecipientEmail());
        verify(objectMapper).convertValue(any(), eq(AdditionalBagInfoDto.class));
    }

    @Test
    void testGetAdditionalBagsInfoThrowsException() {
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ubsManagementService.getAdditionalBagsInfo(1L));
    }

    @Test
    void testSetOrderDetail() {
        Order order = getOrdersStatusDoneDto();

        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(paymentRepository.selectSumPaid(1L)).thenReturn(5000L);

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
        verify(paymentRepository, times(2)).selectSumPaid(1L);
    }

    @Test
    void testSetOrderDetailNeedToChangeStatusToHalfPaid() {
        Order order = getOrdersStatusDoneDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);

        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderRepository.findSumOfCertificatesByOrderId(1L)).thenReturn(10L);
        when(orderBagService.findAllBagsInOrderBagsList(anyList())).thenReturn(ModelUtils.TEST_BAG_LIST2);

        doNothing().when(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.HALF_PAID.name());
        doNothing().when(eventService).saveEvent(anyString(), anyString(), any());

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
        verify(paymentRepository, times(2)).selectSumPaid(1L);
        verify(eventService, times(2)).saveEvent(anyString(), anyString(), any());
    }

    @Test
    void testSetOrderDetailNeedToChangeStatusToUnpaid() {
        Order order = getOrdersStatusDoneDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(paymentRepository.selectSumPaid(1L)).thenReturn(null);
        doNothing().when(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.UNPAID.name());
        when(orderBagService.findAllBagsInOrderBagsList(anyList())).thenReturn(ModelUtils.TEST_BAG_LIST2);

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);

        verify(orderRepository).getOrderDetails(1L);
        verify(paymentRepository).selectSumPaid(1L);
    }

    @Test
    void testSetOrderDetailWhenSumPaidIsNull() {
        Order order = getOrdersStatusDoneDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(paymentRepository.selectSumPaid(1L)).thenReturn(null);

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
        verify(paymentRepository).selectSumPaid(1L);
    }

    @Test
    void testSetOrderDetailIfHalfPaid() {
        Order order = getOrder();
        Order orderDetailDto = getOrdersStatusFormedDto();

        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(orderDetailDto));
        when(paymentRepository.selectSumPaid(1L)).thenReturn(0L);
        when(orderRepository.findSumOfCertificatesByOrderId(1L)).thenReturn(0L);
        when(orderBagService.findAllBagsInOrderBagsList(anyList())).thenReturn(ModelUtils.TEST_BAG_LIST2);

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
        verify(paymentRepository, times(2)).selectSumPaid(1L);
        verify(orderRepository).findSumOfCertificatesByOrderId(1L);
    }

    @Test
    void testSetOrderDetailIfPaidAndPriceLessThanDiscount() {
        Order order = ModelUtils.getOrdersStatusAdjustmentDto2();

        when(certificateRepository.findCertificate(order.getId()))
            .thenReturn(List.of(ModelUtils.getActiveCertificateWith600Points()));
        when(orderRepository.findSumOfCertificatesByOrderId(order.getId())).thenReturn(600L);
        when(orderRepository.getOrderDetails(1L))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto2()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");
        verify(certificateRepository).save(ModelUtils.getActiveCertificateWith600Points().setPoints(0));
        verify(userRepository).updateUserCurrentPoints(1L, 100);
        verify(orderRepository).updateOrderPointsToUse(1L, 0);
    }

    @Test
    void testSetOrderDetailIfPaidAndPriceLessThanPaidSum() {
        Order order = ModelUtils.getOrdersStatusAdjustmentDto2();
        when(paymentRepository.selectSumPaid(order.getId())).thenReturn(10000L);
        when(certificateRepository.findCertificate(order.getId()))
            .thenReturn(List.of(ModelUtils.getActiveCertificateWith600Points()));
        when(orderRepository.findSumOfCertificatesByOrderId(order.getId())).thenReturn(600L);
        when(orderRepository.getOrderDetails(1L))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto2()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(paymentRepository, times(2)).selectSumPaid(order.getId());
        verify(certificateRepository).findCertificate(order.getId());
        verify(orderRepository).findSumOfCertificatesByOrderId(order.getId());
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailConfirmed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusConfirmedDto()));
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
                .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        Order order = getOrdersStatusConfirmedDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
                UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
                UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
                "test@gmail.com");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailConfirmed2() {
        Order order = getOrdersStatusConfirmedDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderDetailRepository.ifRecordExist(1L, 1L)).thenReturn(1L);
        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "test@gmail.com");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(bagRepository, times(2)).findActiveBagById(1);
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository, times(0)).updateExporter(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailWithExportedWaste() {
        Order order = getOrdersStatusDoneDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderDetailRepository.ifRecordExist(any(), any())).thenReturn(1L);

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "test@gmail.com");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(bagRepository, times(2)).findActiveBagById(1);
        verify(orderDetailRepository, times(2)).ifRecordExist(any(), any());
        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailFormed() {
        Order order = getOrdersStatusFormedDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "test@gmail.com");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailFormedWithBagNoPresent() {
        Order order = getOrdersStatusFormedDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "test@gmail.com");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailNotTakenOut() {
        Order order = getOrdersStatusNotTakenOutDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailOnTheRoute() {
        Order order = getOrdersStatusOnThe_RouteDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailsDone() {
        Order order = getOrdersStatusDoneDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderBroughtItHimself() {
        Order order = getOrdersStatusBROUGHT_IT_HIMSELFDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void testSetOrderDetailsCanceled() {
        Order order = getOrdersStatusCanceledDto();
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.setOrderDetail(order,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(bagRepository, times(2)).findCapacityById(1);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void setOrderDetailExceptionTest() {
        Order order = getOrder();
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.setOrderDetail(order, confirm, exported, "test@gmail.com"));
    }

    @Test
    void testSetOrderDetailThrowsUserNotFoundException() {
        Order order = getOrder();
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.setOrderDetail(order, confirm, exported, "test@gmail.com"));
    }

    @Test
    void testSaveAdminToOrder() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));

        ubsManagementService.saveAdminCommentToOrder(getAdminCommentDto(), "test@gmail.com");

        verify(orderRepository).findById(1L);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
    }

    @Test
    void testUpdateEcoNumberForOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrder()));
        ubsManagementService.updateEcoNumberForOrderById(getEcoNumberDto(), 1L, "abc");
        verify(orderRepository).findById(1L);
    }

    @Test
    void testUpdateEcoNumberForOrderByIdThrowOrderNotFoundException() {
        EcoNumberDto dto = getEcoNumberDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateEcoNumberForOrderById(dto, 1L, "test@gmail.com"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void testUpdateEcoNumberForOrderByIdTrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        assertThrows(NotFoundException.class,
                () -> ubsManagementService.updateEcoNumberForOrderById(ecoNumberDto, 1L, "abc"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void testUpdateEcoNumberForOrderByIdTrowsIncorrectEcoNumberFormatException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrder()));

        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        ecoNumberDto.setEcoNumber(new HashSet<>(List.of("1234a")));
        assertThrows(BadRequestException.class,
                () -> ubsManagementService.updateEcoNumberForOrderById(ecoNumberDto, 1L, "abc"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void saveAdminCommentThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        AdminCommentDto adminCommentDto = getAdminCommentDto();
        assertThrows(NotFoundException.class,
                () -> ubsManagementService.saveAdminCommentToOrder(adminCommentDto, "abc"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrdersForUserTest() {
        Order order1 = getOrderUserFirst();
        Order order2 = ModelUtils.getOrderUserSecond();
        List<Order> orders = List.of(order1, order2);
        OrderInfoDto info1 = OrderInfoDto.builder().id(1L).orderPrice(10).build();
        OrderInfoDto info2 = OrderInfoDto.builder().id(2L).orderPrice(20).build();
        when(orderRepository.getAllOrdersOfUser("uuid")).thenReturn(orders);
        when(modelMapper.map(order1, OrderInfoDto.class)).thenReturn(info1);
        when(modelMapper.map(order2, OrderInfoDto.class)).thenReturn(info2);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order1));
        when(orderRepository.getOrderDetails(2L)).thenReturn(Optional.of(order2));

        ubsManagementService.getOrdersForUser("uuid");

        verify(orderRepository).getAllOrdersOfUser("uuid");
        verify(modelMapper).map(order1, OrderInfoDto.class);
        verify(modelMapper).map(order2, OrderInfoDto.class);
        verify(orderRepository).getOrderDetails(1L);
        verify(orderRepository).getOrderDetails(2L);
    }

    @Test
    void getOrderStatusDataThrowsUnexistingOrderExceptionTest() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderStatusData(100L, null));
    }

    @Test
    void updateOrderAdminPageInfoTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();

        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));
        when(ubsClientService.updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(),
            "test@gmail.com")).thenReturn(ModelUtils.getUbsCustomersDto());
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        when(orderAddressRepository.findById(1L)).thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        var receivingStation = getReceivingStation();

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en", "test@gmail.com");
        UpdateOrderPageAdminDto emptyDto = new UpdateOrderPageAdminDto();
        ubsManagementService.updateOrderAdminPageInfo(emptyDto, order, "en", "test@gmail.com");

        verify(ubsClientService).updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "test@gmail.com");

        verify(employeeRepository, times(2)).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository, times(2)).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);

        verify(ubsClientService).updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "test@gmail.com");

        verify(orderAddressRepository).findById(1L);
        verify(receivingStationRepository).findAll();
        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void updateOrderAdminPageInfoWithUbsCourierSumAndWriteOffStationSum() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        when(ubsClientService.updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(),
            "test@gmail.com")).thenReturn(ModelUtils.getUbsCustomersDto());

        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setUbsCourierSum(50.);
        updateOrderPageAdminDto.setWriteOffStationSum(100.);

        when(orderAddressRepository.findById(1L)).thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        var receivingStation = getReceivingStation();

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en", "test@gmail.com");
        UpdateOrderPageAdminDto emptyDto = new UpdateOrderPageAdminDto();
        ubsManagementService.updateOrderAdminPageInfo(emptyDto, order, "en", "test@gmail.com");

        verify(ubsClientService).updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "test@gmail.com");

        verify(employeeRepository, times(2)).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository, times(2)).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);

        verify(ubsClientService).updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "test@gmail.com");

        verify(orderAddressRepository).findById(1L);
        verify(receivingStationRepository).findAll();
        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).getOrderDetails(1L);
    }

    @Test
    void updateOrderAdminPageInfoWithStatusFormedTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.FORMED);
        EmployeeOrderPosition employeeOrderPosition = ModelUtils.getEmployeeOrderPosition();
        Employee employee = getEmployee();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithStatusFormed();

        when(orderRepository.save(order)).thenReturn(order);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(List.of(employeeOrderPosition));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en", "test@gmail.com");

        verify(orderRepository, times(2)).save(order);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);
        verify(receivingStationRepository).findAll();
        verify(employeeOrderPositionRepository).findAllByOrderId(1L);
    }

    @Test
    void updateOrderAdminPageInfoWithStatusCanceledTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.CANCELED);
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithStatusCanceled();
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(getEmployee()));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
                "test@gmail.com"));

        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, OrderStatus.CANCELED), exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderAdminPageInfoWithStatusDoneTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.DONE);
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithStatusCanceled();
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(getEmployee()));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
                "test@gmail.com"));

        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, OrderStatus.DONE), exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderAdminPageInfoWithStatusBroughtItHimselfTest() {
        Order order = getOrder();
        LocalDateTime orderDate = LocalDateTime.of(2023, 6, 10, 12, 10);
        LocalDateTime deliverFrom = LocalDateTime.of(2023, 6, 16, 15, 30);
        LocalDateTime deliverTo = LocalDateTime.of(2023, 6, 16, 19, 30);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(orderDate);
        order.setDateOfExport(deliverFrom.toLocalDate());
        order.setDeliverFrom(deliverFrom);
        order.setDeliverTo(deliverTo);
        order.setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        Employee employee = getEmployee();
        UpdateOrderPageAdminDto updateOrderPageAdminDto =
            ModelUtils.updateOrderPageAdminDtoWithStatusBroughtItHimself();

        when(orderRepository.save(order)).thenReturn(order);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en", "test@gmail.com");

        assertEquals(1L, order.getReceivingStation().getId());
        assertEquals(deliverFrom.toLocalDate(), order.getDateOfExport());
        assertEquals(deliverFrom, order.getDeliverFrom());
        assertEquals(deliverTo, order.getDeliverTo());
        verify(orderRepository).save(order);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);
        verifyNoMoreInteractions(orderRepository, employeeRepository, paymentRepository);
    }

    @Test
    void updateOrderAdminPageInfoWithNullFieldsTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        Employee employee = getEmployee();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithNullFields();

        when(orderRepository.save(order)).thenReturn(order);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en", "test@gmail.com");

        verify(orderRepository, times(3)).save(order);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void updateOrderAdminPageInfoTestThrowsException() {
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        Order order = getOrder();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L)).thenReturn(Optional.of(tariffsInfo));
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(
                updateOrderPageAdminDto, order, "en", "test@gmail.com"));
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
    }

    @Test
    void updateOrderAdminPageInfoForOrderWithStatusBroughtItHimselfTest() {
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setGeneralOrderInfo(OrderDetailStatusRequestDto
            .builder()
            .orderStatus(String.valueOf(OrderStatus.DONE))
            .build());

        LocalDateTime orderDate = LocalDateTime.now();
        TariffsInfo tariffsInfo = getTariffsInfo();
        Order expected = getOrder();
        expected.setOrderDate(orderDate).setTariffsInfo(tariffsInfo);
        expected.setOrderStatus(OrderStatus.DONE);

        Order order = getOrder();
        order.setOrderDate(orderDate).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        Employee employee = getEmployee();

        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en", "test@gmail.com");

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        assertEquals(expected, order);
    }

    @Test
    void saveReason() {
        Order order = ModelUtils.getOrdersDto();
        ReasonNotTakeBagDto dto = new ReasonNotTakeBagDto();
        dto.setDescription("uu");
        ubsManagementService.saveReason(order, "uu", new MultipartFile[2]);
        assertEquals(order.getReasonNotTakingBagDescription(), dto.getDescription());
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderSumDetailsForFormedOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = getFormedOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForFormedOrderWithUbsCourierSumAndWriteOffStationSum() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = getFormedOrder();
        order.setOrderDate(LocalDateTime.now());
        order.setUbsCourierSum(50_00L);
        order.setWriteOffStationSum(100_00L);
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
    void getOrderSumDetailsForCanceledPaidOrderWithBags() {
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

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForFormedHalfPaidOrderWithDiffBags() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

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

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsThrowsUnexcitingOrderExceptionTest() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderSumDetails(1L));
    }

    @Test
    void getOrderStatusDataTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(orderStatusTranslationRepository.findAllBy()).thenReturn(getOrderStatusTranslations());
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderPaymentStatusTranslationRepository.getAllBy()).thenReturn(getOrderStatusPaymentTranslations());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(bagRepository).findAllActiveBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(3)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().getFirst(), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verify(orderStatusTranslationRepository).findAllBy();
        verify(orderPaymentStatusTranslationRepository).getAllBy();
    }

    @Test
    void getOrderStatusDataTestEmptyPriceDetails() {
        Order order = getOrderForGetOrderStatusEmptyPriceDetails();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.empty());
        when(modelMapper.map(getBaglist().getFirst(), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(modelMapper.map(getOrderForGetOrderStatusData2Test().getPayment().getFirst(), PaymentInfoDto.class))
            .thenReturn(getInfoPayment());
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(3)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().getFirst(), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());

    }

    @Test
    void getOrderStatusDataWithEmptyCertificateTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().getFirst(), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(1L);
        verify(orderRepository, times(3)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().getFirst(), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusDataWhenOrderTranslationIsNull() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().getFirst(), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(1L);
        verify(orderRepository, times(3)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().getFirst(), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusDataExceptionTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        TariffsInfo tariffsInfo = getTariffsInfo();
        BagInfoDto bagInfoDto = getBagInfoDto();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().getFirst(), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderStatusData(1L, "test@gmail.com"));
    }

    @Test
    void updateOrderExportDetails() {
        User user = getTestUser();
        Order order = getOrderDoneByUser();

        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequestToday();
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetailsById(user.getId(), testDetails, "test@gmail.com");

        assertEquals(OrderStatus.ON_THE_ROUTE, order.getOrderStatus());

        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void updateOrderExportDetailsEmptyDetailsTest() {
        User user = getTestUser();
        Order order = getOrder();
        order.setDeliverFrom(null);
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate emptyDetails = ExportDetailsDtoUpdate.builder().receivingStationId(1L).build();
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetailsById(user.getId(), emptyDetails, user.getUuid());

        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void updateOrderExportDetailsUserNotFoundExceptionTest() {
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetailsById(1L, testDetails, "test@gmail.com"));
    }

    @Test
    void updateOrderExportDetailsNotExistingOrderExceptionTest() {
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetailsById(1L, testDetails, "abc"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderExportDetailsReceivingStationNotFoundExceptionTest() {
        Order order = getOrder();
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetailsById(1L, testDetails, "abc"));

        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void updateAllOrderAdminPageInfoUnexistingOrderExceptionTest() {
        Order order = getOrder();
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto();
        when(orderRepository.findById(4L)).thenReturn(Optional.ofNullable(order));
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua"));
    }

    @Test
    void updateAllOrderAdminPageInfoUpdateAdminPageInfoExceptionTest() {
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(Order.builder().build()));
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua"));
    }

    @Test
    void updateAllOrderAdminPageInfoStatusConfirmedTest() {
        Order order = getOrder();
        order.setOrderDate(LocalDateTime.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(getReceivingStation()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        UpdateAllOrderPageDto expectedObject = updateAllOrderPageDto();
        UpdateAllOrderPageDto actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");
    }

    @Test
    void updateAllOrderAdminPageInfoAdditionalOrdersEmptyTest() {
        Order order = ModelUtils.getOrder2();
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(getReceivingStation()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "test@gmail.com", "ua");

        verify(orderRepository, times(2)).findById(1L);
    }

    @Test
    void testAddPointsToUserWhenCurrentPointIsNull() {
        User user = getTestUser();
        user.setUuid(userRemoteClient.findUuidByEmail(user.getRecipientEmail()));
        user.setCurrentPoints(null);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ubsManagementService.addPointsToUser(AddingPointsToUserDto.builder().additionalPoints(anyInt()).build());

        assertEquals(2L, user.getChangeOfPointsList().size());
    }

    @Test
    void saveReasonWhenListElementsAreNotNulls() {
        Order order = ModelUtils.getOrdersDto();
        ReasonNotTakeBagDto dto = new ReasonNotTakeBagDto();
        dto.setDescription("uu");
        ubsManagementService.saveReason(order, "uu", new MultipartFile[] {
            new MockMultipartFile("Name", new byte[2]), new MockMultipartFile("Name", new byte[2])});
        assertEquals(order.getReasonNotTakingBagDescription(), dto.getDescription());
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderStatusDataWithNotEmptyLists() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = mock(OrderPaymentStatusTranslation.class);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().getFirst(), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(
            orderPaymentStatusTranslationRepository.getAllBy())
            .thenReturn(List.of(orderPaymentStatusTranslation));

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());
        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(3)).findById(1L);
        verify(modelMapper).map(getBaglist().getFirst(), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(
            1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusesTranslationTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = mock(OrderPaymentStatusTranslation.class);
        List<OrderStatusTranslation> list = new ArrayList<>();
        TariffsInfo tariffsInfo = getTariffsInfo();
        list.add(getOrderStatusTranslation());
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().getFirst(), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());

        when(orderStatusTranslationRepository.findAllBy())
            .thenReturn(list);

        when(
            orderPaymentStatusTranslationRepository.getAllBy())
            .thenReturn(List.of(orderPaymentStatusTranslation));

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(3)).findById(1L);
        verify(modelMapper).map(getBaglist().getFirst(), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(
            1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());

    }

    @Test
    void checkEmployeeForOrderTest() {
        User user = getTestUser();
        Order order = user.getOrders().getFirst();
        order.setOrderStatus(OrderStatus.CANCELED).setTariffsInfo(getTariffsInfo());
        Employee employee = getEmployee();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(employee.getId())).thenReturn(tariffsInfoIds);
        assertEquals(true, ubsManagementService.checkEmployeeForOrder(order.getId(), "test@gmail.com"));
    }

    @Test
    void updateOrderStatusToExpected() {
        ZoneId expectedZoneId = ZoneId.of("Europe/Kiev");
        LocalDate expectedLocalDate = LocalDate.of(2019, 1, 2);

        try (MockedStatic<LocalDate> localDate = Mockito.mockStatic(LocalDate.class)) {
            localDate.when(() -> LocalDate.now(expectedZoneId))
                .thenReturn(expectedLocalDate);

            ubsManagementService.updateOrderStatusToExpected();

            verify(orderRepository).updateOrderStatusToExpected(OrderStatus.CONFIRMED.name(),
                OrderStatus.ON_THE_ROUTE.name(),
                expectedLocalDate);
        }
    }

    @ParameterizedTest
    @MethodSource("provideOrdersWithDifferentInitialExportDetailsForUpdateOrderExportDetails")
    void updateOrderExportDetailsSettingForDifferentInitialExportDetailsTest(Order order) {
        Employee employee = getEmployee();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetailsById(order.getId(), testDetails, employee.getEmail());
        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    private static Stream<Arguments> provideOrdersWithDifferentInitialExportDetailsForUpdateOrderExportDetails() {
        Order orderWithoutExportDate = getOrderExportDetails();
        orderWithoutExportDate.setDateOfExport(null);
        Order orderWithoutDeliverFromTo = getOrderExportDetails();
        orderWithoutDeliverFromTo.setDeliverFrom(null);
        orderWithoutDeliverFromTo.setDeliverTo(null);
        String updateExportDetails = String.format(OrderHistory.UPDATE_EXPORT_DATA,
            LocalDate.of(1997, 12, 4)) +
            String.format(OrderHistory.UPDATE_DELIVERY_TIME,
                LocalTime.of(15, 40, 24), LocalTime.of(19, 30, 30))
            +
            String.format(OrderHistory.UPDATE_RECEIVING_STATION, "Петрівка");
        return Stream.of(
            Arguments.of(getOrderExportDetailsWithNullValues(),
                OrderHistory.SET_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithExportDate(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithExportDateDeliverFrom(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithExportDateDeliverFromTo(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetails(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithDeliverFromTo(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(orderWithoutExportDate,
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(orderWithoutDeliverFromTo,
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails));
    }

    @Test
    void updateOrderExportDetailsWhenDeliverFromIsNull() {
        Employee employee = getEmployee();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        Order order = getOrderExportDetails();
        var receivingStation = getReceivingStation();
        order.setDeliverFrom(null);
        testDetails.setTimeDeliveryFrom(null);

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetailsById(order.getId(), testDetails, employee.getEmail());
        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void updateOrderExportDetailsWhenDeliverToIsNull() {
        Employee employee = getEmployee();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        Order order = getOrderExportDetails();
        var receivingStation = getReceivingStation();
        order.setDeliverTo(null);
        testDetails.setTimeDeliveryTo(null);

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetailsById(order.getId(), testDetails, employee.getEmail());
        verify(receivingStationRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(receivingStationRepository).findAll();
    }

    @Test
    void getOrderCancellationReasonTest() {
        OrderCancellationReasonDto cancellationReasonDto = ModelUtils.getCancellationDto();
        Order order = ModelUtils.getOrderTest();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        OrderCancellationReasonDto result = ubsManagementService.getOrderCancellationReason(1L);
        assertEquals(cancellationReasonDto.getCancellationReason(), result.getCancellationReason());
        assertEquals(cancellationReasonDto.getCancellationComment(), result.getCancellationComment());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderCancellationReasonWithoutOrderTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderCancellationReason(1L));
    }

    @Test
    void getNotTakenOrderReasonTest() {
        NotTakenOrderReasonDto notTakenOrderReasonDto = ModelUtils.getNotTakenOrderReasonDto();
        Order order = ModelUtils.getTestNotTakenOrderReason();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        NotTakenOrderReasonDto result = ubsManagementService.getNotTakenOrderReason(1L);

        assertEquals(notTakenOrderReasonDto.getDescription(), result.getDescription());
        assertEquals(notTakenOrderReasonDto.getImages(), result.getImages());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getNotTakenOrderReasonWithoutOrderTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getNotTakenOrderReason(1L));
        verify(orderRepository).findById(1L);
    }

    @Test
    void saveOrderIdForRefundThrowsNotFoundExceptionTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getNotTakenOrderReason(1L));
        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderAdminPageInfoAndSaveReasonTest() {
        var dto = updateOrderPageAdminDto();
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];

        Order order = getOrder();

        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);

        Employee employee = getEmployee();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        when(orderAddressRepository.findById(1L)).thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        var receivingStation = getReceivingStation();

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.updateOrderAdminPageInfoAndSaveReason(1L, dto, "en", "test@gmail.com", multipartFiles);

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);

        verify(orderAddressRepository).findById(1L);
        verify(receivingStationRepository).findAll();

        verify(receivingStationRepository).findById(1L);
    }

    @Test
    void updateOrderAdminPageInfoAndSaveReasonTest_OrderPaid() {
        var dto = updateOrderPageAdminDto();
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];

        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Employee employee = getEmployee();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        when(orderAddressRepository.findById(1L)).thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        var receivingStation = getReceivingStation();

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.updateOrderAdminPageInfoAndSaveReason(1L, dto, "en", "test@gmail.com", multipartFiles);

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);

        verify(orderAddressRepository).findById(1L);
        verify(receivingStationRepository).findAll();

        verify(receivingStationRepository).findById(1L);
    }

    @Test
    void updateOrderAdminPageInfoAndSaveReasonTest_OrderPaidAndUpdate() {
        var dto = updateOrderPageAdminDto();
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];

        Order order = getOrder();
        order.setPointsToUse(-10000);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Employee employee = getEmployee();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        when(orderAddressRepository.findById(1L)).thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        var receivingStation = getReceivingStation();

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.updateOrderAdminPageInfoAndSaveReason(1L, dto, "en", "test@gmail.com", multipartFiles);

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);

        verify(orderAddressRepository).findById(1L);
        verify(receivingStationRepository).findAll();

        verify(receivingStationRepository).findById(1L);
    }

    @Test
    void updateOrderAdminPageInfoAndSaveReasonTest_OrderUnPaidAndUpdate() {
        var dto = updateOrderPageAdminDto();
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];

        Order order = getOrder();
        order.setPointsToUse(-10000);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Employee employee = getEmployee();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(1L, 1L))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        when(orderAddressRepository.findById(1L)).thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        var receivingStation = getReceivingStation();

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));

        ubsManagementService.updateOrderAdminPageInfoAndSaveReason(1L, dto, "en", "test@gmail.com", multipartFiles);

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(1L, 1L);
        verify(paymentRepository).findAllByOrderId(1L);

        verify(orderAddressRepository).findById(1L);
        verify(receivingStationRepository).findAll();

        verify(receivingStationRepository).findById(1L);
    }

    @Test
    void getOrderStatusDataTestIfEmployeeIsNotAdmin() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(orderStatusTranslationRepository.findAllBy()).thenReturn(getOrderStatusTranslations());
        when(orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderPaymentStatusTranslationRepository.getAllBy()).thenReturn(getOrderStatusPaymentTranslations());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(positionRepository.findAllIdsFromNames(any())).thenReturn(List.of(6L, 7L));
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        order.setBlocked(false);
        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderLockService, times(0)).lockOrder(order, employee);

        order.setBlocked(true);
        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderLockService, times(0)).lockOrder(order, employee);
    }

    @Test
    void getOrderStatusDataTestIfEmployeeIsAdmin() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getAdminEmployee();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(orderStatusTranslationRepository.findAllBy()).thenReturn(getOrderStatusTranslations());
        when(orderPaymentStatusTranslationRepository.getById(1L))
            .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderPaymentStatusTranslationRepository.getAllBy()).thenReturn(getOrderStatusPaymentTranslations());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(positionRepository.findAllIdsFromNames(any())).thenReturn(List.of(6L, 7L));
        when(paymentService.getPaymentInfo(anyLong(), anyDouble())).thenReturn(getPaymentTableInfoDto());

        order.setBlocked(true);
        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderLockService, times(0)).lockOrder(order, employee);
    }

    @Test
    void processRefundForOrder_ShouldRefundBonuses_andThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        Employee employee = getEmployee();
        TariffsInfo tariffsInfo = getTariffsInfo();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setReturnBonuses(true);
        order.getPayment().forEach(p -> p.setAmount(-300L));
        order.setPointsToUse(1);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
                "test@gmail.com"));
        assertEquals(USER_HAS_NO_OVERPAYMENT, exception.getMessage());
    }

    @Test
    void processRefundForOrder_ShouldRefundBonuses_andSaveOrder() {
        Order order = getOrderForGetOrderStatusData2Test();
        Employee employee = getEmployee();
        TariffsInfo tariffsInfo = getTariffsInfo();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setReturnBonuses(true);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());
        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
            "test@gmail.com");
        verify(orderRepository).save(any(Order.class));
        verify(userRepository).save(any(User.class));
        verify(eventService).saveEvent(eq(OrderHistory.ADDED_BONUSES), eq("test@gmail.com"), any(Order.class));
        assertEquals(OrderPaymentStatus.PAYMENT_REFUNDED, order.getOrderPaymentStatus());
    }

    @Test
    void processRefundForOrder_ShouldRefundMoney_andThrowsBadRequestException() {
        Order order = getOrderForGetOrderStatusData2Test();
        Employee employee = getEmployee();
        TariffsInfo tariffsInfo = getTariffsInfo();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setReturnMoney(true);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
                "test@gmail.com"));
        assertEquals(INCOMPATIBLE_ORDER_STATUS_FOR_REFUND, exception.getMessage());
    }

    @Test
    void processRefundForOrder_ShouldRefundMoney_andThrowsBadRequestException2() {
        Order order = getOrderForGetOrderStatusData2Test();
        Employee employee = getEmployee();
        TariffsInfo tariffsInfo = getTariffsInfo();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setReturnMoney(true);
        order.setOrderStatus(OrderStatus.CANCELED);
        order.getPayment().forEach(p -> p.setAmount(-300L));
        order.setPointsToUse(1);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
                "test@gmail.com"));
        assertEquals(USER_HAS_NO_OVERPAYMENT, exception.getMessage());
    }

    @Test
    void processRefundForOrder_ShouldRefundMoney_andSaveRefund() {
        Order order = getOrderForGetOrderStatusData2Test();
        Employee employee = getEmployee();
        TariffsInfo tariffsInfo = getTariffsInfo();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setReturnMoney(true);
        order.setOrderStatus(OrderStatus.CANCELED);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, order, "en",
            "test@gmail.com");
        verify(refundRepository).save(any(Refund.class));
        verify(orderRepository).save(any(Order.class));
        verify(eventService).saveEvent(eq(OrderHistory.CANCELED_ORDER_MONEY_REFUND), eq("test@gmail.com"),
            any(Order.class));
        assertEquals(OrderPaymentStatus.PAYMENT_REFUNDED, order.getOrderPaymentStatus());
    }
}
