package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.CourierLocation;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModelUtils {

    public static User getUser() {
        return User.builder()
            .uuid("uuid")
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
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
            .courierLocations(CourierLocation.builder().id(1L).build())
            .orderDate(LocalDateTime.now())
            .build());
        return orderList;
    }

    public static Order getOrder() {
        return Order.builder()
            .orderStatus(OrderStatus.FORMED)
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
            .courierLocations(CourierLocation.builder().id(1L).build())
            .build();
    }

    public static List<Address> getAddresses() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(Address.builder()
            .region("Kyiv")
            .city("Kyiv")
            .street("Ivana Franka")
            .district("Shevchenka")
            .houseNumber("44")
            .houseCorpus("3")
            .entranceNumber("2")
            .addressComment("")
            .build());
        return addresses;
    }
}
