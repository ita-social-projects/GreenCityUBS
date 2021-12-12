package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.order.OrderStatusTranslation;
import greencity.repository.*;
import greencity.service.ubs.OrdersAdminsPageService;
import greencity.service.ubs.OrdersAdminsPageServiceImpl;
import greencity.service.ubs.UBSManagementEmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import javax.persistence.EntityNotFoundException;
import java.util.*;

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

}
