package greencity.service;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.exceptions.employee.EmployeeNotFoundException;
import greencity.exceptions.position.PositionNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.*;
import greencity.service.ubs.EventService;
import greencity.service.ubs.OrdersAdminsPageServiceImpl;
import greencity.service.ubs.UBSManagementEmployeeService;
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
    private UserRepository userRepository;
    @Mock
    EventService eventService;

    @Mock
    private SuperAdminService superAdminService;

    @Mock
    private UserRemoteClient userRemoteClient;

    @InjectMocks
    private OrdersAdminsPageServiceImpl ordersAdminsPageService;

    @Test
    void getParametersForOrdersExceptionTable() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));
        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @Test
    void getParametersForOrdersExceptionTable2() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @Test
    void getParametersForOrdersTest() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation().setNameEng("en");

        List<ReceivingStationDto> receivingStations = new ArrayList<>();
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(ModelUtils.getEmployee());

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1))
                .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(1))
                .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(2))
                .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(2l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(2))
                .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(2l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(3))
                .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(3l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(3))
                .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(3l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(4))
                .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(4l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(5))
                .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(5l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6))
                .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(6l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(7))
                .thenReturn(Optional.ofNullable(orderStatusTranslation2.setStatusId(7l)));

        when(orderStatusTranslationRepository.getOrderStatusTranslationById(8))
                .thenReturn(Optional.ofNullable(orderStatusTranslation.setStatusId(8l)));


        when(superAdminService.getAllReceivingStations())
                .thenReturn(receivingStations);
        when(employeeRepository.getAllEmployeeByPositionId(2L))
                .thenReturn(employeeList);
        when(employeeRepository.getAllEmployeeByPositionId(3L))
                .thenReturn(employeeList);
        when(employeeRepository.getAllEmployeeByPositionId(5L))
                .thenReturn(employeeList);
        when(employeeRepository.getAllEmployeeByPositionId(4L))
                .thenReturn(employeeList);
        assertNotNull(ordersAdminsPageService.getParametersForOrdersTable("1"));
    }

    @ParameterizedTest
    @CsvSource({
        "FORMED, ADJUSTMENT",
        "ADJUSTMENT, CONFIRMED",
        "CONFIRMED, ON_THE_ROUTE",
        "ON_THE_ROUTE, DONE"
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

        assertNull(order.getDateOfExport());
        assertNull(order.getDeliverFrom());
        assertNull(order.getDeliverTo());
        assertNull(order.getReceivingStation());
        assertNull(order.getEmployeeOrderPositions());

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
        String uuid = "uuid";
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(positionRepository.findById(1l)).thenReturn(position);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(employeeOrderPositionRepository.existsByOrderAndPosition(order.get(), position.get()))
            .thenReturn(Boolean.TRUE);
        when(eventService.changesWithResponsibleEmployee(1L, Boolean.TRUE)).thenReturn("Some changes");

        ordersAdminsPageService.responsibleEmployee(List.of(1l), "1", 1l, uuid);

        verify(userRepository).findUserByUuid(uuid);
        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(employeeOrderPositionRepository).existsByOrderAndPosition(order.get(), position.get());
        verify(eventService).changesWithResponsibleEmployee(1L, Boolean.TRUE);
    }

    @Test
    void responsibleEmployeeWithoutExistedBeforeTest() {
        String uuid = "uuid";
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(positionRepository.findById(1l)).thenReturn(position);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(employeeOrderPositionRepository.existsByOrderAndPosition(order.get(), position.get()))
            .thenReturn(Boolean.FALSE);
        when(employeeOrderPositionRepository.findAllByOrderId(1L))
            .thenReturn(new ArrayList<>(Arrays.asList(EmployeeOrderPosition.builder()
                .id(1l)
                .employee(employee.get())
                .order(order.get())
                .position(position.get())
                .build())));
        when(eventService.changesWithResponsibleEmployee(1L, Boolean.FALSE)).thenReturn("Some changes");

        ordersAdminsPageService.responsibleEmployee(List.of(1l), "1", 1l, uuid);

        verify(userRepository).findUserByUuid(uuid);
        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
        verify(employeeOrderPositionRepository).existsByOrderAndPosition(order.get(), position.get());
        verify(employeeOrderPositionRepository).findAllByOrderId(1L);
        verify(eventService).changesWithResponsibleEmployee(1L, Boolean.FALSE);
    }

    @Test
    void responsibleEmployeeThrowsUserNotFoundException() {
        String uuid = "uuid";
        List<Long> ordersId = List.of(1l);

        assertThrows(UserNotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeThrowsEntityNotFoundException() {
        String uuid = "uuid";
        List<Long> ordersId = List.of(1l);
        Optional<User> user = Optional.of(ModelUtils.getUser());
        when(userRepository.findUserByUuid(uuid)).thenReturn(user);

        assertThrows(EmployeeNotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeThrowsPositionNotFoundException() {
        String uuid = "uuid";
        List<Long> ordersId = List.of(1l);
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);

        assertThrows(PositionNotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeCatchException() {
        String uuid = "uuid";
        List<Long> ordersId = List.of(1l);
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(positionRepository.findById(1l)).thenReturn(position);

        ordersAdminsPageService.responsibleEmployee(ordersId, "1", 1L, uuid);

        verify(userRepository).findUserByUuid(uuid);
        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void chooseOrdersDataSwitcherTest() {
        String uuid = "uuid";
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<User> user = Optional.of(ModelUtils.getUser());

        when(receivingStationRepository.getOne(1L)).thenReturn(ModelUtils.getReceivingStation());
        when(userRepository.findByUuid(uuid)).thenReturn(user.get());
        when(employeeRepository.findByEmail(user.get().getRecipientEmail())).thenReturn(employee);

        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);
        dto.setColumnName("dateOfExport");
        dto.setNewValue("2022-12-12");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);
        dto.setColumnName("timeOfExport");
        dto.setNewValue("00:00-00:30");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);
        dto.setColumnName("receivingStation");
        dto.setNewValue("1");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);

        verify(userRepository, atLeast(1)).findByUuid(uuid);
        verify(receivingStationRepository, atLeast(1)).getOne(1L);
        verify(employeeRepository, atLeast(1)).findByEmail(user.get().getRecipientEmail());
    }

    @Test
    void receivingStationForDevelopStage() {
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(orderRepository.findById(1L)).thenReturn(order);
        when(receivingStationRepository.getOne(1L)).thenReturn(ModelUtils.getReceivingStation());

        ordersAdminsPageService.receivingStationForDevelopStage(List.of(1l), "1", 1L);
        verify(orderRepository).save(order.get());
    }

    @Test
    void chooseOrdersDataSwitcherTestForResponsibleEmployee() {
        String uuid = "uuid";
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(positionRepository.findById(1l)).thenReturn(position);

        when(employeeOrderPositionRepository.existsByOrderAndPosition(order.get(), position.get()))
            .thenReturn(Boolean.FALSE);
        when(employeeOrderPositionRepository.findAllByOrderId(1L))
            .thenReturn(new ArrayList<>(Arrays.asList(EmployeeOrderPosition.builder()
                .id(1l)
                .employee(employee.get())
                .order(order.get())
                .position(position.get())
                .build())));
        when(eventService.changesWithResponsibleEmployee(1L, Boolean.FALSE)).thenReturn("Some changes");

        when(userRepository.findByUuid(uuid)).thenReturn(user.get());
        when(employeeRepository.findByEmail(user.get().getRecipientEmail())).thenReturn(employee);
        dto.setColumnName("responsibleManager");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);

        verify(userRepository, atLeast(1)).findByUuid(uuid);
        verify(userRepository, atLeast(1)).findUserByUuid(uuid);
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
        orders.add(1l);

        when(userRemoteClient.findByUuid(user.getUuid())).thenReturn(Optional.of(ModelUtils.getUbsCustomersDto().setEmail("test@gmail.com")));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(orderRepository.findById(1l)).thenReturn(Optional.of(ModelUtils.getOrder()));

        assertNotNull(ordersAdminsPageService.requestToBlockOrder(user.getUuid(), orders));
    }

    @Test
    void unblockOrderTest() {
        User user = ModelUtils.getUser().setUuid("uuid");
        List<Long> orders = new ArrayList<>();
        orders.add(1l);

        when(userRemoteClient.findByUuid(user.getUuid())).thenReturn(Optional.of(ModelUtils.getUbsCustomersDto().setEmail("test@gmail.com")));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(ModelUtils.getEmployee()));
        when(orderRepository.findById(1l)).thenReturn(Optional.of(ModelUtils.getOrder()));

        assertNotNull(ordersAdminsPageService.unblockOrder(user.getUuid(), orders));
    }

}
