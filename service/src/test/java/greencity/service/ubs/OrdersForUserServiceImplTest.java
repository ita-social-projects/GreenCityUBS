package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.enums.SortingOrder;
import greencity.repository.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

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
    void getAllOrders(SortingOrder sortingOrder, String column){
        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), column);
        when(ordersForUserRepository.getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L))
            .thenReturn(Page.empty());
        when(userRepository.getOne(anyLong())).thenReturn(ModelUtils.getUser());
        ordersForUserService.getAllOrders(PageRequest.of(1, 1), 1L, sortingOrder, column);

        verify(ordersForUserRepository).getAllOrdersByUserId(PageRequest.of(10, 10, sort), 1L);
    }

    private static Stream<Arguments> provideSortingOrdersAndColumnsForGetAllOrders(){
        return Stream.of(
                Arguments.of(SortingOrder.ASC, "payment_amount"),
                Arguments.of(SortingOrder.DESC, "payment_amount"),
                Arguments.of(SortingOrder.ASC,"order_status")
        );
    }
}
