package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.entity.order.Order;
import greencity.enums.SortingOrder;
import greencity.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.mockito.Mockito.*;

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
            .thenReturn(new PageImpl<>(List.of(new Order()),PageRequest.of(1,1), 1L));
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());
        ordersForUserService.getAllOrders(PageRequest.of(1, 1), 1L, SortingOrder.ASC, "AA");
        verify(ordersForUserRepository).getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L);
    }

    @Test
    void getAllOrdersASC() {
        Sort sort = Sort.by(Sort.Direction.valueOf(SortingOrder.ASC.toString()), "amount");
        when(ordersForUserRepository.getAllOrdersByUserIdAndAmountASC(PageRequest.of(10, 10, sort), 1L))
                .thenReturn(new PageImpl<>(List.of(new Order()),PageRequest.of(1,1), 1L));
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());
        ordersForUserService.getAllOrders(PageRequest.of(1, 1), 1L, SortingOrder.ASC, "amount");
        verify(ordersForUserRepository).getAllOrdersByUserIdAndAmountASC(PageRequest.of(10, 10, sort), 1L);
    }

    @Test
    void getAllOrdersDESC() {
        Sort sort = Sort.by(Sort.Direction.valueOf(SortingOrder.DESC.toString()), "amount");
        when(ordersForUserRepository.getAllOrdersByUserIdAndAmountDesc(PageRequest.of(10, 10, sort), 1L))
                .thenReturn(new PageImpl<>(List.of(new Order()),PageRequest.of(1,1), 1L));
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());
        ordersForUserService.getAllOrders(PageRequest.of(1, 1), 1L, SortingOrder.DESC, "amount");
        verify(ordersForUserRepository).getAllOrdersByUserIdAndAmountDesc(PageRequest.of(10, 10, sort), 1L);

    }

}
