package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.entity.order.Order;
import greencity.enums.SortingOrder;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersForUserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrdersForUserRepository ordersForUserRepository;

    @InjectMocks
    private OrdersForUserServiceImpl ordersForUserService;

    @ParameterizedTest
    @MethodSource("provideSortingOrdersAndColumnsForGetAllOrders")
    void getAllOrders(SortingOrder sortingOrder, String column) {
        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), column);
        when(ordersForUserRepository.getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L))
            .thenReturn(Page.empty());
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());
        ordersForUserService.getAllOrders(PageRequest.of(1, 1), 1L, sortingOrder, column);

        verify(ordersForUserRepository).getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L);
    }

    private static Stream<Arguments> provideSortingOrdersAndColumnsForGetAllOrders() {
        return Stream.of(
            Arguments.of(SortingOrder.ASC, "payment_amount"),
            Arguments.of(SortingOrder.DESC, "payment_amount"),
            Arguments.of(SortingOrder.ASC, "order_status"),
            Arguments.of(SortingOrder.DESC, "order_status"));
    }

    @Test
    void getAllOrdersWithNotEmptyOrderAndNullSumTotalAmountWithoutDiscounts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Order order = ModelUtils.getOrder();
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        Sort sort = Sort.by(Sort.Direction.valueOf(SortingOrder.DESC.toString()), "payment_amount");

        when(ordersForUserRepository.getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L))
            .thenReturn(page);
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());

        ordersForUserService.getAllOrders(PageRequest.of(1, 1),
            1L, SortingOrder.DESC, "payment_amount");

        verify(ordersForUserRepository).getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L);
        verify(userRepository).getOne(anyLong());
    }

    @Test
    void getAllOrdersWithNotEmptyOrderAndNotNullSumTotalAmountWithoutDiscounts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Order order = ModelUtils.getOrder();
        order.setSumTotalAmountWithoutDiscounts(50_00L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        Sort sort = Sort.by(Sort.Direction.valueOf(SortingOrder.DESC.toString()), "payment_amount");

        when(ordersForUserRepository.getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L))
            .thenReturn(page);
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());

        ordersForUserService.getAllOrders(PageRequest.of(1, 1),
            1L, SortingOrder.DESC, "payment_amount");

        verify(ordersForUserRepository).getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L);
        verify(userRepository).getOne(anyLong());
    }
}
