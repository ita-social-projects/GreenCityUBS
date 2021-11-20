package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.UserOrdersDto;
import greencity.dto.UserWithOrdersDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.UserNotFoundException;
import greencity.repository.OrderRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrdersForUserServiceImpl implements OrdersForUserService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    UBSuserRepository ubSuserRepository;

    @Override
    public UserWithOrdersDto getAllOrders(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        UBSuser ubsUser = ubSuserRepository.findUBSuserByUserId(user.getId());
        String username = ubsUser.getFirstName() + " " + ubsUser.getLastName();
        List<UserOrdersDto> userOrdersDtoList = orderRepository.getAllOrdersByUserId(userId)
            .stream()
            .sorted(Comparator.comparing(Order::getOrderDate))
            .map(this::getAllOrders)
            .collect(Collectors.toList());
        return new UserWithOrdersDto(username, userOrdersDtoList);
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
