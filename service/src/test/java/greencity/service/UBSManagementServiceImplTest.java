package greencity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.ubs.FileService;
import greencity.service.ubs.UBSManagementServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UBSManagementServiceImplTest {
    @Mock
    AddressRepository addressRepository;
    double distance = 2;
    int litres = 1000;

    @Mock
    private FileService fileService;

    @Mock
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

    @Mock
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

    private static Stream<Arguments> provideDistanceAndLitres() {
        return Stream.of(Arguments.of(-1, 5),
            Arguments.of(25, 5),
            Arguments.of(10, -5),
            Arguments.of(10, 20000));
    }

    @ParameterizedTest
    @MethodSource("provideDistanceAndLitres")
    void getClusteredCoordsInvalidParametersTest(double invalidDistance, int invalidLitres) {
        assertThrows(Exception.class, () -> ubsManagementService.getClusteredCoords(invalidDistance, invalidLitres));
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
        Pageable pageable = PageRequest.of(0, 5);
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
        PageableDto<CertificateDtoForSearching> actual = ubsManagementService.getAllCertificates(pageable);
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
        when(violationRepository.findByOrderId(1L)).thenReturn(Optional.of(violation));
        Optional<ViolationDetailInfoDto> actual = ubsManagementService.getViolationDetailsByOrderId(1L);
        verify(violationRepository, times(1)).findByOrderId(1L);

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
        ExportDetailsDtoRequest dto = ModelUtils.getExportDetailsRequest();
        Order order = ModelUtils.getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<ReceivingStation> stations = Arrays.asList(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetails(order.getId(), dto);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void checkStationNotFound() {
        Assertions.assertThrows(UnexistingOrderException.class, () -> {
            ubsManagementService.getOrderExportDetails(100L);
        });
    }

    @Test
    void deleteViolationFromOrderResponsesNotFoundWhenNoViolationInOrder() {
        when(violationRepository.findByOrderId(1l)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResponseStatusException.class, () -> ubsManagementService.deleteViolation(1L));
        verify(violationRepository, times(1)).findByOrderId(1L);
    }

    @Test
    void deleteViolationFromOrderByOrderId() {
        Violation violation = ModelUtils.getViolation();
        Long id = ModelUtils.getViolation().getOrder().getId();
        when(violationRepository.findByOrderId(1l)).thenReturn(Optional.of(violation));
        doNothing().when(violationRepository).deleteById(id);
        ubsManagementService.deleteViolation(id);

        verify(violationRepository, times(1)).deleteById(id);
    }

    @Test
    void saveNewManualPayment() {
        Order order = ModelUtils.getOrderTest();
        Payment payment = ModelUtils.getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .paymentDate("02-08-2021").amount(500l).receiptLink("link").paymentId(1l).build();

        when(orderRepository.findById(1l)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        ubsManagementService.saveNewManualPayment(1l, paymentDetails, null);

        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1l);
    }

    @Test
    void checkDeleteManualPayment() {
        when(paymentRepository.findById(1l)).thenReturn(Optional.of(getManualPayment()));
        doNothing().when(paymentRepository).deletePaymentById(1l);
        doNothing().when(fileService).delete("");
        ubsManagementService.deleteManualPayment(1l);
        verify(paymentRepository, times(1)).findById(1l);
        verify(paymentRepository, times(1)).deletePaymentById(1l);
    }

    @Test
    void checkUpdateManualPayment() {
        when(paymentRepository.findById(1l)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        ubsManagementService.updateManualPayment(1l, getManualPaymentRequestDto(), null);
        verify(paymentRepository, times(1)).findById(1l);
        verify(paymentRepository, times(2)).save(any());
        verify(fileService, times(0)).delete(null);
    }

    @Test
    void checkUpdateManualPaymentWithImage() {
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        when(paymentRepository.findById(1l)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        when(fileService.upload(file)).thenReturn("path");
        ubsManagementService.updateManualPayment(1l, getManualPaymentRequestDto(), file);
        verify(paymentRepository, times(1)).findById(1l);
        verify(paymentRepository, times(2)).save(any());
        verify(fileService, times(1)).delete("path");
        verify(fileService, times(1)).delete("path");
    }

    @Test
    void checkManualPaymentNotFound() {
        ManualPaymentRequestDto manualPaymentRequestDto = getManualPaymentRequestDto();
        when(paymentRepository.findById(1l)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1l, manualPaymentRequestDto, null));
        verify(paymentRepository, times(1)).findById(1l);
    }

    @Test
    void checkReturnOverpaymentInfo() {
        Order order = ModelUtils.getOrder();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        Long sumToPay = 0L;
        assertEquals(0L, ubsManagementService.returnOverpaymentInfo(order.getId(), sumToPay, 1L)
            .getOverpayment());
        assertEquals(AppConstant.PAYMENT_REFUND,
            ubsManagementService.returnOverpaymentInfo(order.getId(), sumToPay, 1L).getPaymentInfoDtos().get(1)
                .getComment());
    }

    @Test
    void checkGetPaymentInfo() {
        Order order = ModelUtils.getOrder();
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
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        ubsManagementService.returnOverpayment(order.getId(), dto);
        assertEquals(2L, order.getPayment().size());
        assertEquals(2L, user.getChangeOfPointsList().size());
        assertEquals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT,
            order.getPayment().get(order.getPayment().size() - 1).getComment());
        assertEquals(dto.getOverpayment(), user.getCurrentPoints().longValue());
        assertEquals(dto.getOverpayment(), order.getPayment().get(order.getPayment().size() - 1).getAmount());
    }

    @Test
    void returnOverpaymentAsMoneyForStatusCancelled() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.CANCELLED);
        OverpaymentInfoRequestDto dto = ModelUtils.getOverpaymentInfoRequestDto();
        dto.setComment(AppConstant.PAYMENT_REFUND);
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        ubsManagementService.returnOverpayment(order.getId(), dto);
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
        order.setOrderStatus(OrderStatus.CANCELLED);
        OverpaymentInfoRequestDto dto = ModelUtils.getOverpaymentInfoRequestDto();
        dto.setComment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT);
        when(orderRepository.findById(order.getId())).thenReturn(
            Optional.ofNullable(order));
        when(userRepository.findUserByOrderId(order.getId())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        ubsManagementService.returnOverpayment(order.getId(), dto);
        assertEquals(3L, user.getChangeOfPointsList().size());
        assertEquals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT,
            order.getPayment().get(order.getPayment().size() - 1).getComment());
        assertEquals(dto.getOverpayment(), order.getPayment().get(order.getPayment().size() - 1).getAmount());
        assertEquals(dto.getBonuses() + dto.getOverpayment(), user.getCurrentPoints().longValue());
    }

    @Test
    void updateOrderDetailStatus() {
        User user = ModelUtils.getTestUser();
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
            .updateOrderDetailStatus(order.getId(), testOrderDetail);
        assertEquals(expectedObject.getOrderStatus(), producedObject.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObject.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObject.getDate());
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
        when(orderRepository.findById(1L)).thenReturn(Optional.of(TEST_ORDER));
        when(addressRepository.save(TEST_ADDRESS)).thenReturn(TEST_ADDRESS);
        when(addressRepository.findById(TEST_ADDRESS.getId())).thenReturn(Optional.of(TEST_ADDRESS));
        when(modelMapper.map(TEST_ADDRESS, OrderAddressDtoResponse.class)).thenReturn(TEST_ORDER_ADDRESS_DTO_RESPONSE);

        OrderAddressDtoResponse actual = ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE);

        assertEquals(TEST_ORDER_ADDRESS_DTO_RESPONSE, actual);

        verify(orderRepository).findById(1L);
        verify(addressRepository).save(TEST_ADDRESS);
        verify(addressRepository).findById(TEST_ADDRESS.getId());
        verify(modelMapper).map(TEST_ADDRESS, OrderAddressDtoResponse.class);
    }

    @Test
    void testUpdateAddressThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundOrderAddressException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE));
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
            new AddingViolationsToUserDto(1L, "violation");
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
        user.setViolationsDescription(new HashMap<>() {
            {
                put(1L, "Some violation");
            }
        });

        ViolationsInfoDto expected = modelMapper.map(user, ViolationsInfoDto.class);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.ofNullable(user));

        ViolationsInfoDto actual = ubsManagementService.getAllUserViolations(user.getRecipientEmail());

        assertEquals(expected, actual);
    }

    @Test
    void checkAddUserViolation() {
        User user = ModelUtils.getTestUser();
        user.setViolations(0);
        user.setViolationsDescription(new HashMap<>());
        Order order = user.getOrders().get(0);
        order.setUser(user);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());

        when(orderRepository.findById(order.getId())).thenReturn(Optional.ofNullable(order));

        ubsManagementService.addUserViolation(add);

        assertEquals(1, user.getViolations());
        assertEquals(add.getViolationDescription(), user.getViolationsDescription().get(order.getId()));
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
        assertEquals(groupedOrderDto.getAmountOfLitres(), 300);
        assertEquals(groupedOrderDto.getGroupOfOrders().get(0).equals(ModelUtils.getOrderDto()), true);
    }

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

        assertThrows(UnexistingUuidExeption.class, () -> ubsManagementService.addPointsToUser(
            AddingPointsToUserDto.builder().additionalPoints(anyInt()).build()));
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

    @Test
    void testUpdatePositionTest() {
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(TEST_EMPLOYEE_ORDER_POSITION);
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER_UPDATE_POSITION));
        when(positionRepository.findById(2L)).thenReturn(Optional.ofNullable(TEST_POSITION));
        when(employeeRepository.findByName(anyString(), anyString())).thenReturn(Optional.ofNullable(TEST_EMPLOYEE));
        when(employeeOrderPositionRepository.saveAll(anyIterable()))
            .thenReturn(TEST_EMPLOYEE_ORDER_POSITION);

        ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE);

        verify(employeeOrderPositionRepository).findAllByOrderId(1L);
        verify(orderRepository).findById(1L);
        verify(positionRepository).findById(2L);
        verify(employeeRepository).findByName(anyString(), anyString());
        verify(employeeOrderPositionRepository).saveAll(anyIterable());
    }

    @Test
    void testUpdatePositionThrowsOrderNotFoundException() {
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(TEST_EMPLOYEE_ORDER_POSITION);
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
            () -> ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE));
    }

    @Test
    void testUpdatePositionThrowsPositionNotFoundException() {
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(TEST_EMPLOYEE_ORDER_POSITION);
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER_UPDATE_POSITION));
        when(positionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(PositionNotFoundException.class,
            () -> ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE));
    }

    @Test
    void testUpdatePositionThrowsEmployeeNotFoundException() {
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(TEST_EMPLOYEE_ORDER_POSITION);
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER_UPDATE_POSITION));
        when(positionRepository.findById(2L)).thenReturn(Optional.of(TEST_POSITION));
        when(employeeRepository.findByName(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
            () -> ubsManagementService.updatePositions(TEST_EMPLOYEE_POSITION_DTO_RESPONSE));
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
        when(updateOrderRepository.updateAmount(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(modelMapper.map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType())).thenReturn(TEST_BAG_MAPPING_DTO_LIST);
        when(bagRepository.findBagByOrderId(1L)).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(TEST_BAG, BagInfoDto.class)).thenReturn(TEST_BAG_INFO_DTO);
        when(bagTranslationRepository.findAllByLanguageOrder("ua", 1L)).thenReturn(TEST_BAG_TRANSLATION_LIST);
        when(modelMapper.map(TEST_BAG_TRANSLATION, BagTransDto.class)).thenReturn(TEST_BAG_TRANS_DTO);
        when(modelMapper.map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()))).thenReturn(TEST_ORDER_DETAILS_INFO_DTO_LIST);

        List<OrderDetailInfoDto> actual = ubsManagementService.setOrderDetail(TEST_UPDATE_ORDER_DETAIL_DTO_LIST, "ua");

        assertEquals(TEST_ORDER_DETAILS_INFO_DTO_LIST, actual);

        verify(updateOrderRepository).updateAmount(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(updateOrderRepository).updateConfirm(anyInt(), anyLong(), anyLong());
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
    void testSetOrderDetailThrowsException() {
        when(updateOrderRepository.updateAmount(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateExporter(anyInt(), anyLong(), anyLong())).thenReturn(true);
        when(updateOrderRepository.updateConfirm(anyInt(), anyLong(), anyLong())).thenReturn(true);

        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.empty());

        assertThrows(UnexistingOrderException.class,
            () -> ubsManagementService.setOrderDetail(TEST_UPDATE_ORDER_DETAIL_DTO_LIST, "ua"));
    }
}
