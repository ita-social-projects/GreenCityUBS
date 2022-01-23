package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.RequestToChangeOrdersDataDTO;
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
import static org.mockito.ArgumentMatchers.any;
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
        RequestToChangeOrdersDataDTO dto = ModelUtils.getRequestToChangeOrdersDataDTO();
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
        dto.setColumnName("");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);

        verify(userRepository, atLeast(1)).findByUuid(uuid);
        verify(receivingStationRepository, atLeast(1)).getOne(1L);
        verify(employeeRepository, atLeast(1)).findByEmail(user.get().getRecipientEmail());
    }

    @Test
    void chooseOrdersDataSwitcherTestForResponsibleEmployee() {
        String uuid = "uuid";
        RequestToChangeOrdersDataDTO dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<User> user = Optional.of(ModelUtils.getUser());
        Optional<Position> position = Optional.of(ModelUtils.getPosition());
        Optional<Order> order = Optional.of(ModelUtils.getOrder());

        when(userRepository.findUserByUuid(uuid)).thenReturn(user);
        when(employeeRepository.findById(1L)).thenReturn(employee);
        when(orderRepository.findById(1L)).thenReturn(order);
        lenient().when(positionRepository.findById(1l)).thenReturn(position);
        lenient().when(positionRepository.findById(2l)).thenReturn(position);
        lenient().when(positionRepository.findById(3l)).thenReturn(position);
        lenient().when(positionRepository.findById(4l)).thenReturn(position);
        lenient().when(positionRepository.findById(5l)).thenReturn(position);
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
        dto.setColumnName("responsibleCaller");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);
        dto.setColumnName("responsibleLogicMan");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);
        dto.setColumnName("responsibleDriver");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);
        dto.setColumnName("responsibleNavigator");
        ordersAdminsPageService.chooseOrdersDataSwitcher(uuid, dto);

        verify(userRepository, atLeast(1)).findByUuid(uuid);
        verify(userRepository, atLeast(1)).findUserByUuid(uuid);
        verify(employeeRepository, atLeast(1)).findById(1L);
        verify(orderRepository, atLeast(1)).findById(1L);
        verify(employeeOrderPositionRepository, atLeast(1)).existsByOrderAndPosition(order.get(), position.get());
        verify(employeeOrderPositionRepository, atLeast(1)).findAllByOrderId(1L);
        verify(eventService, atLeast(1)).changesWithResponsibleEmployee(1L, Boolean.FALSE);
    }
}
