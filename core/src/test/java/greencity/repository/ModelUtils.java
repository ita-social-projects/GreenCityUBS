package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.CourierLocation;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModelUtils {

    public static User getUser() {
        return User.builder()
            .uuid(UUID.randomUUID().toString())
            .orders(getOrderList())
            .currentPoints(0)
            .recipientName("Ivan")
            .recipientSurname("Ivanov")
            .recipientEmail("ivan@gmail.com")
            .recipientPhone("+380981099667")
            .violations(0)
            .build();
    }

    public static List<Order> getOrderList() {
        List<Order> orderList = new ArrayList<>();
        orderList.add(Order.builder()
            .orderStatus(OrderStatus.FORMED)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .user(User.builder().id(1L).build())
            .ubsUser(UBSuser.builder().id(1L).build())
            .courierLocations(CourierLocation.builder().id(1L).build())
            .orderDate(LocalDateTime.now())
            .build());
        return orderList;
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(User.builder()
            .uuid(UUID.randomUUID().toString())
            .orders(getOrderList())
            .currentPoints(0)
            .recipientName("Ivan")
            .recipientSurname("Ivanov")
            .recipientEmail("ivan@gmail.com")
            .recipientPhone("+380981099667")
            .violations(0)
            .build());
        return users;
    }
}
