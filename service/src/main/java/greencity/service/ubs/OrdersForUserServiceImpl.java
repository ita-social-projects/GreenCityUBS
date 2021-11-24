package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.UserOrdersDto;
import greencity.dto.UserWithOrdersDto;
import greencity.entity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.UserNotFoundException;
import greencity.repository.OrderRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
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

    @Override
    public UserWithOrdersDto getAllOrders(Pageable page, Long userId, SortingOrder sortingOrder, String column) {
        String columnAmount = "amount";
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        UBSuser ubsUser = ubSuserRepository.findUBSuserByUserId(user.getId());
        String username = ubsUser.getFirstName() + " " + ubsUser.getLastName();

        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), column);
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
            .amount(order.getPayment().get(0).getAmount())
            .orderDate(order.getOrderDate())
            .orderStatus(order.getOrderStatus())
            .orderPaymentStatus(order.getOrderPaymentStatus())
            .build();
    }
}
