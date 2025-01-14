package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.entity.user.locations.City;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CreateAddressRequestDtoToCity extends AbstractConverter<CreateAddressRequestDto, City> {
    @Override
    protected City convert(CreateAddressRequestDto source) {
        return City.builder()
            .nameUk(source.getCity())
            .nameEn(source.getCityEn())
            .build();
    }
}
