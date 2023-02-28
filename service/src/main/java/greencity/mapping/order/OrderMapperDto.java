/*
package greencity.mapping.order;

import greencity.dto.order.OrderDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderMapperDto extends AbstractConverter<Order, OrderDto> {
    */
/**
     * Method convert {@link Order} to {@link OrderDto}.
     *
     * @return {@link OrderDto}
     *//*

    @Override
    protected OrderDto convert(Order order) {
        return OrderDto.builder()
            .firstName(order.getUbsUser().getFirstName())
            .lastName(order.getUbsUser().getLastName())
            .address(order.getUbsUser().getAddress().getDistrict() + " "
                + order.getUbsUser().getAddress().getStreet() + " "
                + order.getUbsUser().getAddress().getHouseNumber())
            .addressComment(order.getUbsUser().getAddress().getAddressComment())
            .phoneNumber(order.getUbsUser().getPhoneNumber())
            .latitude(order.getUbsUser().getAddress().getCoordinates().getLatitude())
            .longitude(order.getUbsUser().getAddress().getCoordinates().getLongitude())
            .build();
    }
}
*/
