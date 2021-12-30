package greencity.service.ubs;

import greencity.dto.UserOrdersDto;
import greencity.dto.UserWithOrdersDto;
import greencity.dto.UsernameDto;
import greencity.entity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrdersForUserServiceImpl implements OrdersForUserService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    UBSuserRepository ubSuserRepository;
    OrdersForUserRepository ordersForUserRepository;
    PaymentRepository paymentRepository;

    @Override
    public UserWithOrdersDto getAllOrders(Pageable page, Long userId, SortingOrder sortingOrder, String column) {
        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), column);
        String columnAmount = "amount";
        String username = getUsername(userId).getFirstName() + " " + getUsername(userId).getLastName();

        List<UserOrdersDto> userOrdersDtoList = new ArrayList<>();
        if (!column.equals(columnAmount)) {
            userOrdersDtoList = ordersForUserRepository
                .getAllOrdersByUserId(PageRequest.of(page.getPageNumber() * 10, 10, sort), userId)
                .stream()
                .map(this::getAllOrders)
                .collect(Collectors.toList());
        } else if (sortingOrder.toString().equals("DESC")) {
            userOrdersDtoList = ordersForUserRepository
                .getAllOrdersByUserIdAndAmountDesc(PageRequest.of(page.getPageNumber() * 10, 10, sort), userId)
                .stream()
                .map(this::getAllOrders)
                .collect(Collectors.toList());
        } else if (sortingOrder.toString().equals("ASC")) {
            userOrdersDtoList = ordersForUserRepository
                .getAllOrdersByUserIdAndAmountASC(PageRequest.of(page.getPageNumber() * 10, 10, sort), userId)
                .stream()
                .map(this::getAllOrders)
                .collect(Collectors.toList());
        }
        return new UserWithOrdersDto(username, userOrdersDtoList);
    }

    private UserOrdersDto getAllOrders(Order order) {
        return UserOrdersDto.builder()
            .id(order.getId())
            .amount(paymentRepository.findAmountByOrderId(order.getId()))
            .orderDate(order.getOrderDate())
            .orderStatus(order.getOrderStatus())
            .orderPaymentStatus(order.getOrderPaymentStatus())
            .build();
    }

    private UsernameDto getUsername(Long userID) {
        return UsernameDto.builder()
            .firstName(orderRepository.getUsersFirstNameByOrderId(userID))
            .lastName(orderRepository.getUsersLastNameByOrderId(userID))
            .build();
    }
}
