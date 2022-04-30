package greencity.mapping;

import greencity.dto.user.PersonalDataDto;
import greencity.entity.user.User;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link User} into
 * {@link PersonalDataDto}.
 */
@Component
public class UserToPersonalDataDtoMapper extends AbstractConverter<User, PersonalDataDto> {
    /**
     * Method convert {@link User} to {@link PersonalDataDto}.
     *
     * @return {@link PersonalDataDto}
     */
    @Override
    protected PersonalDataDto convert(User user) {
        return PersonalDataDto.builder()
            .id(user.getId())
            .firstName(user.getRecipientName())
            .lastName(user.getRecipientSurname())
            .phoneNumber(user.getRecipientPhone())
            .email(user.getRecipientEmail())
            .build();
    }
}
