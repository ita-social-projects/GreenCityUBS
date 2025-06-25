package greencity.mapping.location;

import greencity.constant.ErrorMessage;
import greencity.dto.CreateAddressRequestDto;
import greencity.entity.user.Region;
import greencity.entity.user.locations.BaseEntityForEnAndUkNames;
import greencity.entity.user.locations.City;
import greencity.entity.user.locations.District;
import greencity.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class AddressRequestDtoToBaseEntityMapper extends BaseEntityAbstractConverter<CreateAddressRequestDto> {
    /**
     * Converts a given {@link CreateAddressRequestDto} to an instance of the
     * specified target class. The target class must be a subclass of
     * {@link BaseEntityForEnAndUkNames}, and can be either {@link District},
     * {@link City}, or {@link Region}. Throws a {@link BadRequestException} if the
     * conversion fails or if the target class is unsupported.
     *
     * @param source      the source DTO containing address information
     * @param targetClass the class of the target entity to convert to
     * @param <T>         the type of the target entity
     * @return an instance of the target class with the corresponding name fields
     *         set
     * @throws BadRequestException if the conversion fails or if the target class is
     *                             unsupported
     */
    @Override
    public <T extends BaseEntityForEnAndUkNames> T convert(CreateAddressRequestDto source, Class<T> targetClass) {
        try {
            if (District.class.isAssignableFrom(targetClass)) {
                return targetClass.cast(District.builder()
                    .nameUk(source.getDistrictUk())
                    .nameEn(source.getDistrictEn())
                    .build());
            }

            if (City.class.isAssignableFrom(targetClass)) {
                return targetClass.cast(City.builder()
                    .nameUk(source.getCityUk())
                    .nameEn(source.getCityEn())
                    .build());
            }

            if (Region.class.isAssignableFrom(targetClass)) {
                return targetClass.cast(Region.builder()
                    .nameUk(source.getRegionUk())
                    .nameEn(source.getRegionEn())
                    .build());
            }

            throw new BadRequestException(ErrorMessage.UNSUPPORTED_TYPE + targetClass.getName());
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessage.MAPPER_ERROR, e);
        }
    }
}
