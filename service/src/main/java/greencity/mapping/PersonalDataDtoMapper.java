package greencity.mapping;

import greencity.dto.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link UBSuser} into
 * {@link PersonalDataDto}.
 */
@Component
public class PersonalDataDtoMapper extends AbstractConverter<UBSuser, PersonalDataDto> {
    /**
     * Method convert {@link UBSuser} to {@link PersonalDataDto}.
     *
     * @return {@link PersonalDataDto}
     */
    @Override
    protected PersonalDataDto convert(UBSuser ubsUser) {
        PersonalDataDto personalDataDto = PersonalDataDto.builder()
            .id(ubsUser.getId())
            .firstName(ubsUser.getFirstName())
            .lastName(ubsUser.getLastName())
            .phoneNumber(ubsUser.getPhoneNumber())
            .email(ubsUser.getEmail())
            .addressComment(ubsUser.getAddress().getComment())
            .build();

        return personalDataDto;
    }
}
