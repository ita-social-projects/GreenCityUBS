package greencity.service.ubs;

import greencity.constant.AppConstant;
import greencity.dto.order.UserOrdersDto;
import greencity.dto.order.UserWithOrdersDto;
import greencity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrdersForUserServiceImpl implements OrdersForUserService {
    private UserRepository userRepository;
    private OrdersForUserRepository ordersForUserRepository;

    @Override
    public UserWithOrdersDto getAllOrders(Pageable page, Long userId, SortingOrder sortingOrder, String column) {
        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), column);
        String username = getUsername(userId);

        List<UserOrdersDto> userOrdersDtoList = ordersForUserRepository
            .getAllOrdersByUserId(PageRequest.of(page.getPageNumber() * 10, 10, sort), userId)
            .stream().map(this::getAllOrders)
            .collect(Collectors.toList());
        return new UserWithOrdersDto(username, userOrdersDtoList);
    }

    private UserOrdersDto getAllOrders(Order order) {
        return UserOrdersDto.builder()
            .id(order.getId())
            .amount(BigDecimal.valueOf(order.getSumTotalAmountWithoutDiscounts())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .orderDate(order.getOrderDate())
            .orderStatus(order.getOrderStatus())
            .orderPaymentStatus(order.getOrderPaymentStatus())
            .build();
    }

    private String getUsername(Long userID) {
        User currentUser = userRepository.getOne(userID);
        return currentUser.getRecipientName() + " " + currentUser.getRecipientSurname();
    }
}