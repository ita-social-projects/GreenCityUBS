package greencity.mapping.courier;

import greencity.dto.courier.CourierUpdateDto;
import greencity.entity.order.Courier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UpdateCourierDtoMapper extends AbstractConverter<Courier, CourierUpdateDto> {
    @Override
    protected CourierUpdateDto convert(Courier source) {
        return CourierUpdateDto.builder()
            .courierId(source.getId())
            .nameUk(source.getNameUk())
            .nameEn(source.getNameEn())
            .build();
    }
}
