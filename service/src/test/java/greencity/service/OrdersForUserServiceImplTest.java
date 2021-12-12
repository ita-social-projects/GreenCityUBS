package greencity.service;

import greencity.entity.enums.SortingOrder;
import greencity.repository.*;
import greencity.service.ubs.OrdersForUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class OrdersForUserServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UBSuserRepository ubSuserRepository;
    @Mock
    private OrdersForUserRepository ordersForUserRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrdersForUserServiceImpl ordersForUserService;

    @Test
    void getAllOrders() {
        Sort sort = Sort.by(Sort.Direction.valueOf(SortingOrder.ASC.toString()), "AA");
        when(ordersForUserRepository.getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L))
            .thenReturn(Page.empty());
        ordersForUserService.getAllOrders(PageRequest.of(1, 1), 1L, SortingOrder.ASC, "AA");

        verify(ordersForUserRepository).getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L);
    }
}
