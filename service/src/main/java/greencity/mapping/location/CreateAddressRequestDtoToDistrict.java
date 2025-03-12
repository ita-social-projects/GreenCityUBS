package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.entity.user.locations.District;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CreateAddressRequestDtoToDistrict extends AbstractConverter<CreateAddressRequestDto, District> {
    @Override
    protected District convert(CreateAddressRequestDto source) {
        return District.builder()
            .nameUk(source.getDistrict())
            .nameEn(source.getDistrictEn())
            .build();
    }
}
