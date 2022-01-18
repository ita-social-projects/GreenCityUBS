package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.entity.order.Order;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.exceptions.EmployeeNotFoundException;
import greencity.exceptions.PositionNotFoundException;
import greencity.exceptions.UserNotFoundException;
import greencity.repository.*;
import greencity.service.ubs.EventService;
import greencity.service.ubs.OrdersAdminsPageServiceImpl;
import greencity.service.ubs.UBSManagementEmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private RestClient restClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    EventService eventService;

    @InjectMocks
    private OrdersAdminsPageServiceImpl ordersAdminsPageService;

    @Test
    void getParametersForOrdersExceptionTable() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(1, 1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));
        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable(1L));
    }

    @Test
    void getParametersForOrdersExceptionTable2() {

        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderStatusTranslation orderStatusTranslation2 = ModelUtils.getOrderStatusTranslation();

        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(1, 1L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation));

        when(orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(1, 2L))
            .thenReturn(Optional.ofNullable(orderStatusTranslation2));

        assertThrows(EntityNotFoundException.class, () -> ordersAdminsPageService.getParametersForOrdersTable(1L));
    }

    @Test
    void orderStatusForDevelopStage() {
        Order order = ModelUtils.getOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        ordersAdminsPageService.orderStatusForDevelopStage(List.of(1L), "aa", 1L);
        verify(orderRepository).findById(1L);
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

        assertThrows(UserNotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(List.of(1l), "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeThrowsEntityNotFoundException() {
        String uuid = "uuid";
        Optional<User> user = Optional.of(ModelUtils.getUser());
        when(userRepository.findUserByUuid(uuid)).thenReturn(user);

        assertThrows(EmployeeNotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(List.of(1l), "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeThrowsPositionNotFoundException() {
        String uuid = "uuid";
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);

        assertThrows(PositionNotFoundException.class,
            () -> ordersAdminsPageService.responsibleEmployee(List.of(1l), "1", 1L, uuid));
    }

    @Test
    void responsibleEmployeeCatchException() {
        String uuid = "uuid";
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(positionRepository.findById(1l)).thenReturn(position);

        ordersAdminsPageService.responsibleEmployee(List.of(1l), "1", 1L, uuid);

        verify(userRepository).findUserByUuid(uuid);
        verify(employeeRepository).findById(1L);
        verify(orderRepository).findById(1L);
    }
}
