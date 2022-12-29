package greencity.mapping.courier;

import greencity.dto.courier.CreateCourierDto;
import greencity.entity.order.Courier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CreateCourierDtoMapper extends AbstractConverter<Courier, CreateCourierDto> {
    @Override
    protected CreateCourierDto convert(Courier source) {
        return CreateCourierDto.builder()
                .nameEn(source.getNameEn())
                .nameUk(source.getNameUk())
                .build();
    }
}
