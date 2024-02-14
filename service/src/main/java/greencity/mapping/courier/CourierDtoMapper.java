package greencity.mapping.courier;

import greencity.dto.courier.CourierDto;
import greencity.entity.order.Courier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class CourierDtoMapper extends AbstractConverter<Courier, CourierDto> {
    @Override
    protected CourierDto convert(Courier source) {
        LocalDate createdAt = source.getCreateDate();
        return CourierDto.builder()
            .courierId(source.getId())
            .courierStatus(source.getCourierStatus().toString())
            .nameEn(source.getNameEn())
            .nameUk(source.getNameUk())
            .createDate(source.getCreateDate() != null ? createdAt : LocalDate.now())
            .createdBy(source.getCreatedBy().getFirstName() + " "
                + source.getCreatedBy().getLastName())
            .build();
    }
}
