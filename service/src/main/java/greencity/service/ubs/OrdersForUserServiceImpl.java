package greencity.service.ubs;

import greencity.dto.UserOrdersDto;
import greencity.dto.UserWithOrdersDto;
import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrdersForUserServiceImpl implements OrdersForUserService {
    OrderRepository orderRepository;

    @Override
    public UserWithOrdersDto getAllOrders(Long userId) {
        List<UserOrdersDto> userOrdersDtoList = orderRepository.getAllOrdersByUserId(userId)
            .stream()
            .sorted(Comparator.comparing(Order::getOrderDate))
            .map(this::getAllOrders)
            .collect(Collectors.toList());
        return new UserWithOrdersDto(userOrdersDtoList);
    }

    private UserOrdersDto getAllOrders(Order order) {
        return UserOrdersDto.builder()
            .id(order.getId())
            .amount(order.getPayment().get(0).getAmount())
            .orderDate(order.getOrderDate())
            .orderStatus(order.getOrderStatus())
            .orderPaymentStatus(order.getOrderPaymentStatus())
            .build();
    }
}
