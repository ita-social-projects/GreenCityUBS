package greencity.mapping;

import greencity.dto.OrderDetailDto;
import greencity.dto.OrderDetailInfoDto;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderDelailMapper extends AbstractConverter<OrderDetailDto, List<OrderDetailInfoDto>> {
    @Override
    protected List<OrderDetailInfoDto> convert(OrderDetailDto orderDetailDto) {
        List<OrderDetailInfoDto> dto = new ArrayList<>();
        for (int i = 0; i < orderDetailDto.getCapacityAndPrice().size(); i++) {
            dto.add(OrderDetailInfoDto.builder()
                .confirmedQuantity(orderDetailDto.getAmount().get(i).getConfirmed())
                .exportedQuantity(orderDetailDto.getAmount().get(i).getExported())
                .amount(orderDetailDto.getAmount().get(i).getAmount())
                .capacity(orderDetailDto.getCapacityAndPrice().get(i).getCapacity())
                .price(orderDetailDto.getCapacityAndPrice().get(i).getPrice())
                .orderId(orderDetailDto.getOrderId())
                .name(orderDetailDto.getName().get(i).getName())
                .bagId(orderDetailDto.getCapacityAndPrice().get(i).getId())
                .build());
        }
        return dto;
    }
}