package greencity.mapping;

import greencity.dto.OrderDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderMapperDto extends AbstractConverter<Order, OrderDto> {
    /**
     * Method convert {@link Order} to {@link OrderDto}.
     *
     * @return {@link OrderDto}
     */
    @Override
    protected OrderDto convert(Order order) {
        return OrderDto.builder()
            .firstName(order.getUbsUser().getFirstName())
            .lastName(order.getUbsUser().getLastName())
            .address(order.getUbsUser().getUserAddress().getDistrict() + " "
                + order.getUbsUser().getUserAddress().getStreet() + " "
                + order.getUbsUser().getUserAddress().getHouseNumber())
            .addressComment(order.getUbsUser().getUserAddress().getComment())
            .phoneNumber(order.getUbsUser().getPhoneNumber())
            .latitude(order.getUbsUser().getUserAddress().getCoordinates().getLatitude())
            .longitude(order.getUbsUser().getUserAddress().getCoordinates().getLongitude())
            .build();
    }
}
