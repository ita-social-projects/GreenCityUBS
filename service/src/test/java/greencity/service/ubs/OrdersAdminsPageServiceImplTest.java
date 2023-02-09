package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.SuperAdminService;
import greencity.service.notification.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersAdminsPageServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UBSManagementEmployeeService employeeService;
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
    @InjectMocks
    private OrdersAdminsPageServiceImpl ordersAdminsPageService;

    @Test
    void getParametersForOrdersExceptionTable() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));
        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @Test
    void getParametersForOrdersExceptionTable2() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable("1"));
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

        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @Test
    void getParametersForOrdersTest() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation().setNameEng("en");
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

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(9L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(9L)));

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
        var order = ModelUtils.getOrder();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        var changeOrderResponseDto = ordersAdminsPageService.chooseOrdersDataSwitcher(
            email,
            requestToChangeOrdersDataDto);

        assertEquals(
            0,
            changeOrderResponseDto.getUnresolvedGoalsOrderId().size());

        int orderRepositoryFindByIdCalls = requestToChangeOrdersDataDto.getOrderIdsList().size();

        verify(employeeRepository).findByEmail(email);
        verify(orderRepository, times(orderRepositoryFindByIdCalls)).findById(anyLong());
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

        ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), newStatus, 1L);

        assertNotNull(order.getDateOfExport());
        assertNotNull(order.getDeliverFrom());
        assertNotNull(order.getDeliverTo());
        assertNotNull(order.getReceivingStation());
        assertNotNull(order.getEmployeeOrderPositions());

        verify(orderRepository).save(order);
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

        ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), newStatus, 1L);

        verify(orderRepository).save(order);
    }

    @Test
    void orderStatusForDevelopStageWithEmptyOrderId() {
        ordersAdminsPageService.orderStatusForDevelopStage(Collections.emptyList(), "", 1L);

        verify(orderRepository).changeStatusForAllOrders("", 1L);
        verify(orderRepository).unblockAllOrders(1L);
    }

    @Test
    void orderStatusForDevelopStageEntityNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(List.of(1L),
            ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), "", 1L));
    }

    @Test
    void orderStatusForDevelopStageBadOrderStatusRequestException() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(ModelUtils.getOrder().setOrderStatus(OrderStatus.FORMED)));
        assertEquals(List.of(1L),
            ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), "DONE", 1L));
    }

    @Test
    void responsibleEmployeeWithExistedBeforeTest() {
        String email = "test@gmail.com";
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(employeeRepository.findById(1L)).thenReturn(employee);
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
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(employeeRepository.findById(1L)).thenReturn(employee);
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
    void responsibleEmployeeThrowsEntityNotFoundException() {
        String email = "test@gmail.com";
        List<Long> ordersId = List.of(1L);

        assertThrows(NotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, email));
    }

    @Test
    void responsibleEmployeeThrowsPositionNotFoundException() {
        String uuid = "uuid";
        List<Long> ordersId = List.of(1L);
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());

        when(employeeRepository.findById(1L)).thenReturn(employee);

        assertThrows(NotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeCatchException() {
        String email = "test@gmail.com";
        List<Long> ordersId = List.of(1L);
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());

        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(positionRepository.findById(1L)).thenReturn(position);

        ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, email);

        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void chooseOrdersDataSwitcherTest() {
        String email = "test@gmail.com";
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());

        when(receivingStationRepository.getOne(1L)).thenReturn(ModelUtils.getReceivingStation());
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

        verify(receivingStationRepository, atLeast(1)).getOne(1L);
        verify(employeeRepository, atLeast(1)).findByEmail(email);
    }

    @Test
    void receivingStationForDevelopStage() {
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(orderRepository.findById(1L)).thenReturn(order);
        when(receivingStationRepository.getOne(1L)).thenReturn(ModelUtils.getReceivingStation());

        ordersAdminsPageService.receivingStationForDevelopStage(List.of(1L), "1", 1L);
        verify(orderRepository).save(order.get());
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

}
