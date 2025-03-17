package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.api.RegionInfoDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.table.ColumnWidthDto;
import greencity.dto.user.ChatLinkDto;
import greencity.entity.order.*;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.BonusReason;
import greencity.enums.CancellationReason;
import greencity.enums.OrderStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.SuperAdminService;
import greencity.service.notification.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersAdminsPageServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ReceivingStationRepository receivingStationRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private EmployeeOrderPositionRepository employeeOrderPositionRepository;
    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;
    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    EventService eventService;
    @Mock
    NotificationServiceImpl notificationService;
    @Mock
    private SuperAdminService superAdminService;
    @Mock
    private UserRemoteClient userRemoteClient;
    @Mock
    private TableColumnWidthForEmployeeRepository tableColumnWidthForEmployeeRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private OrderAddressRepository orderAddressRepository;
    @InjectMocks
    private OrdersAdminsPageServiceImpl ordersAdminsPageService;
    private static final String EMAIL = "test@email.com";
    private static final String ADDRESS_COMMENT = "commentToAddressForClient";
    private static final String CLIENT_COMMENT = "commentForOrderByClient";
    private static final String ORDER_COMMENT = "commentsForOrder";
    private static final String CHAT_LINK = "https://my.binotel.ua/f/chat/#/visitor/21269249.12893974";

    @Test
    void getParametersForOrdersExceptionTable() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));
        assertThrowsEntityNotFoundException();
    }

    @Test
    void getParametersForOrdersExceptionTable2() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        assertThrowsEntityNotFoundException();
    }

    @Test
    void getParametersForOrdersExceptionTable3() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation();

        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(ModelUtils.getEmployee());

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(2L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(2L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(2L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(2L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(3L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(3L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(3L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(3L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(4L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(4L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(5L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(5L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(6L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(7L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(7L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(8L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(8L)));

        assertThrowsEntityNotFoundException();
    }

    void assertThrowsEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
            () -> ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @Test
    void getParametersForOrdersTest() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation().setNameEn("en");
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = ModelUtils.getOrderPaymentStatusTranslation();

        List<ReceivingStationDto> receivingStations = new ArrayList<>();
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(ModelUtils.getEmployee());

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(2L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(2L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(2L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(2L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(3L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(3L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(3L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(3L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(4L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(4L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(5L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(5L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(6L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(7L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(7L)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(8L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(8L)));

        when(orderPaymentStatusTranslationRepository.getOrderPaymentStatusTranslationById(anyLong()))
            .thenReturn(Optional.ofNullable(orderPaymentStatusTranslation));

        when(superAdminService.getAllReceivingStations())
            .thenReturn(receivingStations);
        when(employeeRepository.findAllByEmployeePositionId(2L))
            .thenReturn(employeeList);
        when(employeeRepository.findAllByEmployeePositionId(3L))
            .thenReturn(employeeList);
        when(employeeRepository.findAllByEmployeePositionId(5L))
            .thenReturn(employeeList);
        when(employeeRepository.findAllByEmployeePositionId(4L))
            .thenReturn(employeeList);
        assertNotNull(ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @Test
    void chooseOrdersDataSwitcherEmptyOrdersIdListThrowExceptionTest() {
        var requestToChangeOrdersDataDTO = RequestToChangeOrdersDataDto.builder()
            .columnName("columnName")
            .newValue("newValue")
            .build();

        var currentException = assertThrows(BadRequestException.class,
            () -> ordersAdminsPageService.chooseOrdersDataSwitcher(ModelUtils.TEST_EMAIL,
                requestToChangeOrdersDataDTO));

        assertEquals(ErrorMessage.EMPTY_ORDERS_ID_COLLECTION, currentException.getMessage());
    }

    @Test
    void timeOfExportForDevelopStageTest() {
        var ordersId = List.of(1L);
        var newValue = "10:30-15:00";
        var employeeId = 3L;
        LocalDate exportDate = LocalDate.of(2023, 5, 23);

        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(employeeId).build())
            .dateOfExport(exportDate)
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<Long> result = ordersAdminsPageService.timeOfExportForDevelopStage(ordersId, newValue, employeeId);

        verify(orderLockService).unlockOrder(order);

        assertTrue(result.isEmpty());
    }

    @Test
    void timeOfExportForDevelopStageNotSetExportDateThrowExceptionTest() {
        var orderId = 1L;
        var ordersId = List.of(orderId);
        var newValue = "10:30-15:00";
        var employeeId = 3L;

        var order = Order.builder()
            .id(orderId)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(employeeId).build())
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.timeOfExportForDevelopStage(ordersId, newValue, employeeId);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(employeeId, order.getBlockedByEmployee().getId());
        assertNull(order.getDeliverFrom());
        assertNull(order.getDeliverTo());
        assertEquals(List.of(orderId), result);
    }

    @Test
    void timeOfExportForDevelopStageOrderBlockedByAnotherEmployeeThrowExceptionTest() {
        var orderId = 1L;
        var ordersId = List.of(orderId);
        var newValue = "10:30-15:00";
        var employeeId = 3L;
        var anotherEmployeeId = 4L;
        LocalDate exportDate = LocalDate.of(2023, 5, 23);

        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(anotherEmployeeId).build())
            .dateOfExport(exportDate)
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.timeOfExportForDevelopStage(ordersId, newValue, employeeId);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(anotherEmployeeId, order.getBlockedByEmployee().getId());
        assertNull(order.getDeliverFrom());
        assertNull(order.getDeliverTo());
        assertEquals(List.of(orderId), result);
    }

    @Test
    void dateOfExportForDevelopStageUpdateDeliveringTimeTest() {
        var ordersId = List.of(1L);
        var newValue = "2023-06-30T00:00:00.000Z";
        LocalTime timeFrom = LocalTime.parse("10:30", DateTimeFormatter.ISO_TIME);
        LocalTime timeTo = LocalTime.parse("15:00", DateTimeFormatter.ISO_TIME);
        var employeeId = 3L;
        LocalDate previousExportDate = LocalDate.of(2023, 5, 23);

        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(employeeId).build())
            .dateOfExport(previousExportDate)
            .deliverFrom(LocalDateTime.of(previousExportDate, timeFrom))
            .deliverTo(LocalDateTime.of(previousExportDate, timeTo))
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.dateOfExportForDevelopStage(ordersId, newValue, employeeId);

        verify(orderLockService).unlockOrder(order);

        assertTrue(result.isEmpty());
    }

    @Test
    void dateOfExportForDevelopStageBlockedByAnotherEmployeeThrowExceptionTest() {
        var orderId = 1L;
        var ordersId = List.of(orderId);
        var newValue = "2023-06-30T00:00:00.000Z";
        var employeeId = 3L;
        var anotherEmployeeId = 4L;
        LocalDate exportDate = LocalDate.of(2023, 5, 23);

        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(anotherEmployeeId).build())
            .dateOfExport(exportDate)
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.dateOfExportForDevelopStage(ordersId, newValue, employeeId);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(anotherEmployeeId, order.getBlockedByEmployee().getId());
        assertEquals(exportDate, order.getDateOfExport());
        assertEquals(List.of(orderId), result);
    }

    @Test
    void chooseOrderDataSwitcherThrowEntityNotFoundExceptionTest() {
        String email = ModelUtils.TEST_EMAIL;
        var requestToChangeOrdersDataDto = ModelUtils.getRequestToChangeOrdersDataDTO();
        var entityNotFoundException = assertThrows(
            EntityNotFoundException.class,
            () -> ordersAdminsPageService.chooseOrdersDataSwitcher(email, requestToChangeOrdersDataDto));

        assertEquals(ErrorMessage.EMPLOYEE_NOT_FOUND, entityNotFoundException.getMessage());

        verify(employeeRepository).findByEmail(email);
    }

    @Test
    void adminCommentForDevelopStageReturnNotEmptyList() {
        String email = ModelUtils.TEST_EMAIL;
        var requestToChangeOrdersDataDto = ModelUtils.getRequestToAddAdminCommentForOrder();
        var employee = ModelUtils.getEmployee();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        var changeOrderResponseDTO = ordersAdminsPageService.chooseOrdersDataSwitcher(
            email,
            requestToChangeOrdersDataDto);

        assertEquals(
            requestToChangeOrdersDataDto.getOrderIdsList().size(),
            changeOrderResponseDTO.getUnresolvedGoalsOrderId().size());

        int orderRepositoryFindByIdCalls = requestToChangeOrdersDataDto.getOrderIdsList().size();

        verify(employeeRepository).findByEmail(email);
        verify(orderRepository, times(orderRepositoryFindByIdCalls)).findById(anyLong());
    }

    @Test
    void adminCommentForDevelopStageReturnEmptyList() {
        String email = ModelUtils.TEST_EMAIL;
        var requestToChangeOrdersDataDto = ModelUtils.getRequestToAddAdminCommentForOrder();
        var employee = ModelUtils.getEmployee();
        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(employee)
            .events(new ArrayList<>())
            .cancellationReason(CancellationReason.DELIVERED_HIMSELF)
            .build();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        doNothing().when(orderLockService).unlockOrder(order);

        var changeOrderResponseDto = ordersAdminsPageService.chooseOrdersDataSwitcher(
            email,
            requestToChangeOrdersDataDto);

        assertEquals(
            0,
            changeOrderResponseDto.getUnresolvedGoalsOrderId().size());
        assertEquals(1, order.getEvents().size());

        int orderRepositoryFindByIdCalls = requestToChangeOrdersDataDto.getOrderIdsList().size();

        verify(employeeRepository).findByEmail(email);
        verify(orderRepository, times(orderRepositoryFindByIdCalls)).findById(anyLong());
        verify(orderLockService, times(orderRepositoryFindByIdCalls)).unlockOrder(any(Order.class));

    }

    @Test
    void chooseOrdersDataSwitcherAdminCommentBlockedByAnotherEmployeeThrowExceptionTest() {
        Long orderId = 1L;
        String email = "test@gmail.com";

        Employee employee = Employee.builder()
            .id(1L)
            .email(email)
            .build();
        Employee anotherEmployee = Employee.builder()
            .id(2L)
            .build();

        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToAddAdminCommentForOrder();

        Order order = Order.builder()
            .id(orderId)
            .blocked(true)
            .blockedByEmployee(anotherEmployee)
            .build();

        ChangeOrderResponseDTO expectedResult = ChangeOrderResponseDTO.builder()
            .httpStatus(HttpStatus.OK)
            .unresolvedGoalsOrderId(List.of(orderId))
            .build();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);

        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(anotherEmployee, order.getBlockedByEmployee());
        assertNull(order.getAdminComment());
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
        "FORMED, ADJUSTMENT",
        "ADJUSTMENT, CONFIRMED",
        "CONFIRMED, ON_THE_ROUTE",
        "ON_THE_ROUTE, DONE",
    })
    void orderStatusForDevelopStage(String oldStatus, String newStatus) {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.valueOf(oldStatus))
            .setDateOfExport(LocalDate.now())
            .setDeliverFrom(LocalDateTime.now())
            .setDeliverTo(LocalDateTime.now())
            .setEmployeeOrderPositions(Set.of(new EmployeeOrderPosition(
                1L,
                ModelUtils.getEmployee(),
                ModelUtils.getPosition(),
                order)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), newStatus, ModelUtils.getEmployee());

        assertNotNull(order.getDateOfExport());
        assertNotNull(order.getDeliverFrom());
        assertNotNull(order.getDeliverTo());
        assertNotNull(order.getReceivingStation());
        assertNotNull(order.getEmployeeOrderPositions());

        verify(orderLockService).unlockOrder(order);
    }

    @Test
    void orderStatusForDevelopStageChangeStatusToBroughtItHimselfTest() {
        String newStatus = "BROUGHT_IT_HIMSELF";
        Order saved = ModelUtils.getOrder();
        saved.setOrderStatus(OrderStatus.FORMED);

        Order expected = ModelUtils.getOrder();
        expected.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        expected.setDateOfExport(null);
        expected.setDeliverFrom(null);
        expected.setDeliverTo(null);
        expected.setReceivingStation(null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(saved));

        ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), newStatus, ModelUtils.getEmployee());

        verify(eventService).save(eq(OrderHistory.ORDER_BROUGHT_IT_HIMSELF_UK), anyString(), any(Order.class));
        verify(notificationService).notifySelfPickupOrder(expected);
        verify(orderLockService).unlockOrder(expected);
    }

    @Test
    void orderStatusForDevelopStageTest_ChangeOrderStatusFromFormedToCanceled() {
        long orderId = 1;
        List<Long> orderIdsList = List.of(orderId);
        String newStatus = "CANCELED";
        Order order = spy(ModelUtils.getOrder());
        order.setOrderStatus(OrderStatus.FORMED);
        int pointsToUse = 100;
        int currentUserPoints = 200;
        Set<Certificate> certificates = Set.of(
            new Certificate());
        User user = Mockito.mock(User.class);
        List<ChangeOfPoints> changeOfPointsList = spy(new ArrayList<>());
        ArgumentCaptor<ChangeOfPoints> argumentCaptor = ArgumentCaptor.forClass(ChangeOfPoints.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getPointsToUse())
            .thenReturn(pointsToUse);
        when(order.getUser())
            .thenReturn(user);
        when(user.getCurrentPoints())
            .thenReturn(currentUserPoints);
        when(user.getChangeOfPointsList())
            .thenReturn(changeOfPointsList);
        when(order.getCertificates())
            .thenReturn(certificates);

        ordersAdminsPageService.orderStatusForDevelopStage(orderIdsList, newStatus, ModelUtils.getEmployee());

        verify(orderRepository).findById(orderId);
        verify(notificationService).notifyBonusesFromCanceledOrder(order);
        verify(user).setCurrentPoints(currentUserPoints + pointsToUse);
        verify(changeOfPointsList).add(argumentCaptor.capture());

        ChangeOfPoints changeOfPoints = argumentCaptor.getValue();
        assertEquals(pointsToUse, changeOfPoints.getAmount());
        assertEquals(LocalDateTime.now().toLocalDate(), changeOfPoints.getDate().toLocalDate());
        assertEquals(BonusReason.REFUND_CANCELED_ORDER, changeOfPoints.getReason());
        assertEquals(user, changeOfPoints.getUser());
        assertEquals(order, changeOfPoints.getOrder());

        verify(userRepository).save(user);
        verify(certificateRepository, times(certificates.size()))
            .save(any(Certificate.class));
        verify(orderLockService).unlockOrder(order);
    }

    @ParameterizedTest
    @CsvSource({
        "CONFIRMED, FORMED",
        "FORMED, BROUGHT_IT_HIMSELF",
        "ON_THE_ROUTE, NOT_TAKEN_OUT",
        "FORMED, CANCELED"
    })
    void orderStatusForDevelopStageErasedPickUpDetailsAndResponsibleEmployees(String oldStatus, String newStatus) {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(OrderStatus.valueOf(oldStatus))
            .setDateOfExport(LocalDate.now())
            .setDeliverFrom(LocalDateTime.now())
            .setDeliverTo(LocalDateTime.now())
            .setEmployeeOrderPositions(Set.of(new EmployeeOrderPosition(
                1L,
                ModelUtils.getEmployee(),
                ModelUtils.getPosition(),
                order)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), newStatus, ModelUtils.getEmployee());

        verify(orderLockService).unlockOrder(order);
    }

    @Test
    void orderStatusForDevelopStageEntityNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(List.of(1L),
            ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), "", ModelUtils.getEmployee()));
    }

    @Test
    void orderStatusForDevelopStageBadOrderStatusRequestException() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(ModelUtils.getOrder().setOrderStatus(OrderStatus.FORMED)));
        assertEquals(List.of(1L),
            ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), "DONE", ModelUtils.getEmployee()));
    }

    @Test
    void orderStatusForDevelopStageBlockedByAnotherEmployeeThrowExceptionTest() {
        var orderId = 1L;
        var ordersId = List.of(orderId);
        var newValue = "CONFIRMED";
        var anotherEmployeeId = 4L;

        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(anotherEmployeeId).build())
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.orderStatusForDevelopStage(ordersId, newValue, ModelUtils.getEmployee());

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(anotherEmployeeId, order.getBlockedByEmployee().getId());
        assertNull(order.getOrderStatus());
        assertEquals(List.of(orderId), result);
    }

    @Test
    void responsibleEmployeeWithExistedBeforeTest() {
        String email = "test@gmail.com";
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Employee> currentEmployee = Optional.of(Employee.builder().id(2L).build());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(employeeRepository.findByEmail(anyString())).thenReturn(currentEmployee);
        when(positionRepository.findById(1L)).thenReturn(position);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(employeeOrderPositionRepository.existsByOrderAndPosition(order.get(), position.get()))
            .thenReturn(Boolean.TRUE);
        when(eventService.changesWithResponsibleEmployee(1L, Boolean.TRUE)).thenReturn("Some changes");

        ordersAdminsPageService.responsibleEmployee(List.of(1L), "1", 1L, email);

        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(employeeOrderPositionRepository).existsByOrderAndPosition(order.get(), position.get());
        verify(eventService).changesWithResponsibleEmployee(1L, Boolean.TRUE);
    }

    @Test
    void responsibleEmployeeWithoutExistedBeforeTest() {
        String email = "test@gmail.com";
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Employee> currentEmployee = Optional.of(Employee.builder().id(2L).build());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(employeeRepository.findByEmail(anyString())).thenReturn(currentEmployee);
        when(positionRepository.findById(1L)).thenReturn(position);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(employeeOrderPositionRepository.existsByOrderAndPosition(order.get(), position.get()))
            .thenReturn(Boolean.FALSE);
        when(employeeOrderPositionRepository.findAllByOrderId(1L))
            .thenReturn(new ArrayList<>(List.of(EmployeeOrderPosition.builder()
                .id(1L)
                .employee(employee.get())
                .order(order.get())
                .position(position.get())
                .build())));
        when(eventService.changesWithResponsibleEmployee(1L, Boolean.FALSE)).thenReturn("Some changes");

        ordersAdminsPageService.responsibleEmployee(List.of(1L), "1", 1L, email);

        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(employeeOrderPositionRepository).existsByOrderAndPosition(order.get(), position.get());
        verify(employeeOrderPositionRepository).findAllByOrderId(1L);
        verify(eventService).changesWithResponsibleEmployee(1L, Boolean.FALSE);
    }

    @Test
    void responsibleEmployeeThrowsUserNotFoundException() {
        String email = "test@gmail.com";
        List<Long> ordersId = List.of(1L);

        assertThrows(NotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, email));
    }

    @Test
    void responsibleEmployeeThrowsPositionNotFoundException() {
        String uuid = "uuid";
        List<Long> ordersId = List.of(1L);
        Optional<Employee> currentEmployee = Optional.of(Employee.builder().id(2L).build());

        when(employeeRepository.findByEmail(anyString())).thenReturn(currentEmployee);

        assertThrows(NotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeCatchException() {
        String email = "test@gmail.com";
        List<Long> ordersId = List.of(1L);
        Optional<Employee> currentEmployee = Optional.of(Employee.builder().id(2L).build());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());

        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(positionRepository.findById(1L)).thenReturn(position);
        when(employeeRepository.findByEmail(email)).thenReturn(currentEmployee);

        ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, email);

        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void responsibleEmployeeOrderBlockedByAnotherEmployee() {
        String email = "test@gmail.com";
        Optional<Employee> currentEmployee = Optional.of(Employee.builder().id(2L).build());
        Optional<Employee> blockedByEmployee = Optional.of(Employee.builder().id(3L).build());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        var ordersIds = List.of(1L);
        Order order = ModelUtils.getOrder();
        order.setBlocked(true);
        order.setBlockedByEmployee(blockedByEmployee.get());

        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(employeeRepository.findByEmail(email)).thenReturn(currentEmployee);
        when(positionRepository.findById(1L)).thenReturn(position);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.responsibleEmployee(ordersIds, "1", 1L, email);

        verify(employeeRepository).findByEmail(email);
        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventService, never()).save(anyString(), anyString(), any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(blockedByEmployee.get(), order.getBlockedByEmployee());
        assertEquals(ordersIds, result);
    }

    @Test
    void chooseOrdersDataSwitcherTest() {
        String email = "test@gmail.com";
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());

        when(receivingStationRepository.getReferenceById(1L)).thenReturn(ModelUtils.getReceivingStation());
        when(employeeRepository.findByEmail(email)).thenReturn(employee);

        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        dto.setColumnName("dateOfExport");
        dto.setNewValue("2022-12-12");
        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        dto.setColumnName("timeOfExport");
        dto.setNewValue("00:00-00:30");
        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        dto.setColumnName("receivingStation");
        dto.setNewValue("1");
        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        dto.setColumnName("cancellationReason");
        dto.setNewValue("MOVING_OUT");
        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        dto.setColumnName("cancellationComment");
        dto.setNewValue("Comment");
        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        dto.setColumnName("adminComment");
        dto.setNewValue("Admin Comment");

        verify(receivingStationRepository, atLeast(1)).getReferenceById(1L);
        verify(employeeRepository, atLeast(1)).findByEmail(email);
    }

    @Test
    void receivingStationForDevelopStage() {
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(orderRepository.findById(1L)).thenReturn(order);
        when(receivingStationRepository.getReferenceById(1L)).thenReturn(ModelUtils.getReceivingStation());

        ordersAdminsPageService.receivingStationForDevelopStage(List.of(1L), "1", 1L);
        verify(orderLockService).unlockOrder(order.get());
    }

    @Test
    void receivingStationForDevelopStageBlockedByAnotherEmployeeThrowExceptionTest() {
        var orderId = 1L;
        var ordersId = List.of(orderId);
        var newValue = "2";
        var employeeId = 3L;
        var anotherEmployeeId = 4L;

        var order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedByEmployee(Employee.builder()
                .id(anotherEmployeeId).build())
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.receivingStationForDevelopStage(ordersId, newValue, employeeId);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));

        assertNull(order.getReceivingStation());
        assertTrue(order.isBlocked());
        assertEquals(anotherEmployeeId, order.getBlockedByEmployee().getId());
        assertEquals(List.of(orderId), result);
    }

    @Test
    void chooseOrdersDataSwitcherTestForResponsibleEmployee() {
        String email = "test@gmail.com";
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(positionRepository.findById(1L)).thenReturn(position);

        when(employeeOrderPositionRepository.existsByOrderAndPosition(order.get(), position.get()))
            .thenReturn(Boolean.FALSE);
        when(employeeOrderPositionRepository.findAllByOrderId(1L))
            .thenReturn(new ArrayList<>(List.of(EmployeeOrderPosition.builder()
                .id(1L)
                .employee(employee.get())
                .order(order.get())
                .position(position.get())
                .build())));
        when(eventService.changesWithResponsibleEmployee(1L, Boolean.FALSE)).thenReturn("Some changes");

        when(employeeRepository.findByEmail(email)).thenReturn(employee);
        dto.setColumnName("responsibleManager");
        ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);

        verify(employeeRepository, atLeast(1)).findById(1L);
        verify(orderRepository, atLeast(1)).findById(1L);
        verify(employeeOrderPositionRepository, atLeast(1)).existsByOrderAndPosition(order.get(), position.get());
        verify(employeeOrderPositionRepository, atLeast(1)).findAllByOrderId(1L);
        verify(eventService, atLeast(1)).changesWithResponsibleEmployee(1L, Boolean.FALSE);
    }

    @Test
    void chooseOrdersDataSwitcherCancellationReasonTest() {
        Long orderId = 1L;
        String email = "test@gmail.com";
        Employee employee = Employee.builder()
            .id(1L)
            .email(email)
            .build();
        RequestToChangeOrdersDataDto dto = RequestToChangeOrdersDataDto.builder()
            .columnName("cancellationReason")
            .newValue("DELIVERED_HIMSELF")
            .orderIdsList(List.of(orderId))
            .build();
        Order order = Order.builder()
            .id(orderId)
            .blocked(true)
            .blockedByEmployee(employee)
            .build();

        ChangeOrderResponseDTO expectedResult = ChangeOrderResponseDTO.builder()
            .httpStatus(HttpStatus.OK)
            .unresolvedGoalsOrderId(Collections.emptyList())
            .build();
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderLockService).unlockOrder(order);

        var result = ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);

        verify(orderLockService).unlockOrder(order);

        assertEquals(CancellationReason.DELIVERED_HIMSELF, order.getCancellationReason());
        assertEquals(expectedResult, result);
    }

    @Test
    void chooseOrdersDataSwitcherCancellationReasonBlockedByAnotherEmployeeThrowExceptionTest() {
        Long orderId = 1L;
        String email = "test@gmail.com";
        Employee employee = Employee.builder()
            .id(1L)
            .email(email)
            .build();
        Employee anotherEmployee = Employee.builder()
            .id(2L)
            .build();

        RequestToChangeOrdersDataDto dto = RequestToChangeOrdersDataDto.builder()
            .columnName("cancellationReason")
            .newValue("DELIVERED_HIMSELF")
            .orderIdsList(List.of(orderId))
            .build();

        Order order = Order.builder()
            .id(orderId)
            .blocked(true)
            .blockedByEmployee(anotherEmployee)
            .build();

        ChangeOrderResponseDTO expectedResult = ChangeOrderResponseDTO.builder()
            .httpStatus(HttpStatus.OK)
            .unresolvedGoalsOrderId(List.of(orderId))
            .build();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);

        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(anotherEmployee, order.getBlockedByEmployee());
        assertNull(order.getCancellationReason());
        assertEquals(expectedResult, result);
    }

    @Test
    void chooseOrdersDataSwitcherCancellationCommentTest() {
        Long orderId = 1L;
        var newComment = "some comment";
        String email = "test@gmail.com";

        Clock clock = Clock.fixed(Instant.parse("2023-05-25T10:15:30.00Z"), ZoneId.of("UTC"));
        LocalDateTime dateTime = LocalDateTime.now(clock);

        Employee employee = Employee.builder()
            .id(1L)
            .email(email)
            .build();

        RequestToChangeOrdersDataDto dto = RequestToChangeOrdersDataDto.builder()
            .columnName("cancellationComment")
            .newValue(newComment)
            .orderIdsList(List.of(orderId))
            .build();

        Order order = Order.builder()
            .id(orderId)
            .blocked(true)
            .blockedByEmployee(employee)
            .events(new ArrayList<>())
            .cancellationReason(CancellationReason.DELIVERED_HIMSELF)
            .build();

        Order expectedSavedOrder = Order.builder()
            .id(orderId)
            .events(new ArrayList<>())
            .cancellationReason(CancellationReason.DELIVERED_HIMSELF)
            .cancellationComment(newComment)
            .build();

        Event event = Event.builder()
            .order(expectedSavedOrder)
            .eventDate(dateTime)
            .authorNameUk(employee.getFirstName() + "  " + employee.getLastName())
            .eventNameUk(OrderHistory.ORDER_CANCELLED_UK + "  " + newComment)
            .build();

        expectedSavedOrder.getEvents().add(event);

        ChangeOrderResponseDTO expectedResult = ChangeOrderResponseDTO.builder()
            .httpStatus(HttpStatus.OK)
            .unresolvedGoalsOrderId(Collections.emptyList())
            .build();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderLockService).unlockOrder(order);

        ChangeOrderResponseDTO result;
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            localDateTime.when(LocalDateTime::now).thenReturn(dateTime);

            result = ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);
        }

        verify(orderLockService).unlockOrder(order);

        assertEquals(List.of(event), order.getEvents());
        assertEquals(expectedResult, result);
    }

    @Test
    void chooseOrdersDataSwitcherCancellationCommentBlockedByAnotherEmployeeThrowExceptionTest() {
        Long orderId = 1L;
        String email = "test@gmail.com";
        var newComment = "some comment";

        Employee employee = Employee.builder()
            .id(1L)
            .email(email)
            .build();
        Employee anotherEmployee = Employee.builder()
            .id(2L)
            .build();

        RequestToChangeOrdersDataDto dto = RequestToChangeOrdersDataDto.builder()
            .columnName("cancellationComment")
            .newValue(newComment)
            .orderIdsList(List.of(orderId))
            .build();

        Order order = Order.builder()
            .id(orderId)
            .blocked(true)
            .blockedByEmployee(anotherEmployee)
            .build();

        ChangeOrderResponseDTO expectedResult = ChangeOrderResponseDTO.builder()
            .httpStatus(HttpStatus.OK)
            .unresolvedGoalsOrderId(List.of(orderId))
            .build();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = ordersAdminsPageService.chooseOrdersDataSwitcher(email, dto);

        verify(orderRepository, never()).save(any(Order.class));

        assertTrue(order.isBlocked());
        assertEquals(anotherEmployee, order.getBlockedByEmployee());
        assertNull(order.getCancellationComment());
        assertEquals(expectedResult, result);
    }

    @Test
    void requestToBlockOrderTest() {
        User user = ModelUtils.getUser().setUuid("uuid");
        List<Long> orders = new ArrayList<>();
        orders.add(1L);

        when(userRemoteClient.findByUuid(user.getUuid()))
            .thenReturn(Optional.of(ModelUtils.getUbsCustomersDto().setEmail("test@gmail.com")));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrder()));

        assertNotNull(ordersAdminsPageService.requestToBlockOrder(user.getUuid(), orders));
    }

    @Test
    void unblockOrderTest() {
        User user = ModelUtils.getUser().setUuid("uuid");
        List<Long> orders = new ArrayList<>();
        orders.add(1L);

        when(userRemoteClient.findByUuid(user.getUuid()))
            .thenReturn(Optional.of(ModelUtils.getUbsCustomersDto().setEmail("test@gmail.com")));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrder()));

        assertNotNull(ordersAdminsPageService.unblockOrder(user.getUuid(), orders));
    }

    @Test
    void getOrderColumnWidthForEmployeeTest() {
        Employee employee = ModelUtils.getEmployee();
        TableColumnWidthForEmployee tableColumnWidthForEmployee = ModelUtils.getTestTableColumnWidth();

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(anyLong()))
            .thenReturn(Optional.ofNullable(tableColumnWidthForEmployee));

        ordersAdminsPageService.getColumnWidthForEmployee("Test");
        verify(employeeRepository).findByUuid("Test");
        verify(tableColumnWidthForEmployeeRepository).findByEmployeeId(1L);
    }

    @Test
    void getOrderColumnWidthNotFoundTableColumnWidthForEmployeeTest() {
        Employee employee = ModelUtils.getEmployee();
        TableColumnWidthForEmployee tableColumnWidthForEmployee =
            new TableColumnWidthForEmployee(ModelUtils.getEmployee());

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(anyLong()))
            .thenReturn(Optional.empty());

        ordersAdminsPageService.getColumnWidthForEmployee("Test");

        verify(modelMapper).map(tableColumnWidthForEmployee, ColumnWidthDto.class);
    }

    @Test
    void saveOrderColumnWidthForEmployeeIfPresentTest() {
        Employee employee = ModelUtils.getEmployee();
        ColumnWidthDto columnWidthDto = ModelUtils.getTestColumnWidthDto();

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(anyLong()))
            .thenReturn(Optional.ofNullable(ModelUtils.getTestTableColumnWidth()));
        when(tableColumnWidthForEmployeeRepository.save(any(TableColumnWidthForEmployee.class)))
            .thenReturn(ModelUtils.getTestTableColumnWidth());

        ordersAdminsPageService.saveColumnWidthForEmployee(columnWidthDto, "Test");
        verify(employeeRepository).findByUuid("Test");
        verify(tableColumnWidthForEmployeeRepository).findByEmployeeId(1L);
        verify(tableColumnWidthForEmployeeRepository).save(any(TableColumnWidthForEmployee.class));
    }

    @Test
    void saveOrderColumnWidthForEmployeeIfNotPresentTest() {
        Employee employee = ModelUtils.getEmployee();
        ColumnWidthDto columnWidthDto = ModelUtils.getTestColumnWidthDto();
        TableColumnWidthForEmployee tableColumnWidthForEmployee = ModelUtils.getTestTableColumnWidth();

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(anyLong())).thenReturn(Optional.empty());
        when(tableColumnWidthForEmployeeRepository.save(any(TableColumnWidthForEmployee.class)))
            .thenReturn(tableColumnWidthForEmployee);
        when(modelMapper.map(columnWidthDto, TableColumnWidthForEmployee.class))
            .thenReturn(tableColumnWidthForEmployee);

        ordersAdminsPageService.saveColumnWidthForEmployee(columnWidthDto, "Test");
        verify(employeeRepository).findByUuid("Test");
        verify(modelMapper).map(columnWidthDto, TableColumnWidthForEmployee.class);
        verify(tableColumnWidthForEmployeeRepository).findByEmployeeId(1L);
        verify(tableColumnWidthForEmployeeRepository).save(any(TableColumnWidthForEmployee.class));
    }

    @Test
    void getAllLocationsInfoTest() {
        when(regionRepository.findAllRegionsWithCitiesAndDistricts())
            .thenReturn(List.of(ModelUtils.getRegionForAllLocationsTest()));

        List<RegionInfoDto> result = ordersAdminsPageService.getAllLocationsInfo();

        assertNotNull(result);
        verify(regionRepository).findAllRegionsWithCitiesAndDistricts();
    }

    @ParameterizedTest
    @ValueSource(strings = "0")
    @NullSource
    void ignoreSetResponsibleEmployeeTest(String employee) {
        List<Long> unResolvedGoals =
            ordersAdminsPageService.responsibleEmployee(List.of(1L), employee, 1L, "test@gmail.com");

        assertTrue(unResolvedGoals.isEmpty());
    }

    @Test
    void removeResponsibleEmployeeTest() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(positionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getPosition()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getOrder()));
        when(employeeOrderPositionRepository.existsByOrderAndPosition(any(Order.class), any(Position.class)))
            .thenReturn(Boolean.TRUE);
        doNothing().when(employeeOrderPositionRepository).delete(any(Order.class), any(Position.class));
        when(eventService.changesWithResponsibleEmployee(anyLong(), eq(Boolean.TRUE))).thenReturn("some changes");
        doNothing().when(orderLockService).unlockOrder(any(Order.class));
        doNothing().when(eventService).saveEvent(anyString(), anyString(), any(Order.class));

        ordersAdminsPageService.responsibleEmployee(List.of(1L), "-1", 1L, "test@gmail.com");

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(positionRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(employeeOrderPositionRepository).existsByOrderAndPosition(any(Order.class), any(Position.class));
        verify(employeeOrderPositionRepository).delete(any(Order.class), any(Position.class));
        verify(eventService).changesWithResponsibleEmployee(1L, true);
        verify(orderLockService).unlockOrder(any(Order.class));
        verify(eventService).saveEvent(anyString(), anyString(), any(Order.class));
    }

    @Test
    void removeResponsibleEmployeeWithErrorsTest() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(positionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getPosition()));
        when(orderRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        List<Long> unresolvedGoals = ordersAdminsPageService.responsibleEmployee(List.of(1L), "-1", 1L, "test@gmail.com");

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(positionRepository).findById(1L);
        verify(orderRepository).findById(1L);

        assertEquals(1, unresolvedGoals.size());
    }

    @Test
    void remove() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(positionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getPosition()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getOrder()));
        when(employeeOrderPositionRepository.existsByOrderAndPosition(any(Order.class), any(Position.class)))
            .thenReturn(Boolean.FALSE);

        ordersAdminsPageService.responsibleEmployee(List.of(1L), "-1", 1L, "test@gmail.com");

        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(positionRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(employeeOrderPositionRepository).existsByOrderAndPosition(any(Order.class), any(Position.class));
    }

    @Test
    void addNewAddressCommentTest() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getOrder()));
        when(orderAddressRepository.findByOrderId(anyLong())).thenReturn(Optional.of(ModelUtils.getOrderAddress()));

        ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(ADDRESS_COMMENT));

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
        verify(orderAddressRepository).findByOrderId(anyLong());
        verify(orderAddressRepository).save(any(OrderAddress.class));
        verify(orderLockService).unlockOrder(any(Order.class));
        verify(eventService).save(anyString(), anyString(), any(Order.class));
    }

    @Test
    void addNewAddressCommentWithErrorsTest() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(ADDRESS_COMMENT));

        assertEquals(1, changeOrderResponseDTO.getUnresolvedGoalsOrderId().size());
        assertEquals(HttpStatus.OK, changeOrderResponseDTO.getHttpStatus());

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
    }

    @Test
    void addNewAddressCommentWithErrorsTest2() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getOrder()));
        when(orderAddressRepository.findByOrderId(anyLong())).thenReturn(null);

        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(ADDRESS_COMMENT));

        assertEquals(1, changeOrderResponseDTO.getUnresolvedGoalsOrderId().size());
        assertEquals(HttpStatus.OK, changeOrderResponseDTO.getHttpStatus());

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
        verify(orderAddressRepository).findByOrderId(anyLong());
    }

    @Test
    void addNewCommentWhenOrderIsBlockedTest() {
        Order order = ModelUtils.getOrder();
        Employee adminEmployee = ModelUtils.getAdminEmployee();
        adminEmployee.setId(2L);
        order.setBlockedByEmployee(adminEmployee);

        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(ADDRESS_COMMENT));

        assertEquals(1, changeOrderResponseDTO.getUnresolvedGoalsOrderId().size());
        assertEquals(HttpStatus.OK, changeOrderResponseDTO.getHttpStatus());

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DONE", "CANCELED", "BROUGHT_IT_HIMSELF"})
    void addNewCommentWithDifferentOrderStatusTest(OrderStatus orderStatus) {
        Order order = ModelUtils.getOrder();
        order.setOrderStatus(orderStatus);

        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(ADDRESS_COMMENT));

        assertEquals(1, changeOrderResponseDTO.getUnresolvedGoalsOrderId().size());
        assertEquals(HttpStatus.OK, changeOrderResponseDTO.getHttpStatus());

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {CLIENT_COMMENT, ORDER_COMMENT})
    void addNewCommentTest(String columnName) {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getOrder()));

        ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(columnName));

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
        verify(orderLockService).unlockOrder(any(Order.class));
        verify(eventService).save(anyString(), anyString(), any(Order.class));
    }

    @Test
    void addNewCommentWithNotExistingOrderTest() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(ModelUtils.getEmployee()));
        when(orderRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(EMAIL, ModelUtils.getChangeRequest(ORDER_COMMENT));

        assertEquals(1, changeOrderResponseDTO.getUnresolvedGoalsOrderId().size());
        assertEquals(HttpStatus.OK, changeOrderResponseDTO.getHttpStatus());

        verify(employeeRepository).findByEmail(anyString());
        verify(orderRepository).findById(anyLong());
    }

    @Test
    void addChatLinkToUserTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getUser()));

        ordersAdminsPageService
            .addChatLinkToUser(ModelUtils.getChatLinkDto(CHAT_LINK));

        verify(userRepository).findById(anyLong());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addChatLinkToUserWithNotExistingUserTest() {
        ChatLinkDto chatLinkDto = ModelUtils.getChatLinkDto(CHAT_LINK);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ordersAdminsPageService.addChatLinkToUser(chatLinkDto));
    }

    @Test
    void addChatLinkToUserWithEmptyLinkTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(ModelUtils.getUser()));

        ordersAdminsPageService.addChatLinkToUser(ModelUtils.getChatLinkDto(""));

        verify(userRepository).findById(anyLong());
        verify(userRepository).save(any(User.class));
    }
}
