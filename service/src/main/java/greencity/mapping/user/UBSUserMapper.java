package greencity.mapping.user;

import greencity.dto.user.PersonalDataDto;
import greencity.entity.user.ubs.UBSUser;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link PersonalDataDto} into
 * {@link UBSUser}.
 */
@Component
public class UBSUserMapper extends AbstractConverter<PersonalDataDto, UBSUser> {
    /**
     * Method convert {@link PersonalDataDto} to {@link UBSUser}.
     *
     * @return {@link UBSUser}
     */
    @Override
    protected UBSUser convert(PersonalDataDto personalDataDto) {
        return UBSUser.builder()
            .id(personalDataDto.getId())
            .firstName(personalDataDto.getFirstName())
            .lastName(personalDataDto.getLastName())
            .email(personalDataDto.getEmail())
            .phoneNumber(personalDataDto.getPhoneNumber())
            .senderFirstName(personalDataDto.getSenderFirstName())
            .senderLastName(personalDataDto.getSenderLastName())
            .senderPhoneNumber(personalDataDto.getSenderPhoneNumber())
            .senderEmail(personalDataDto.getSenderEmail())
            .build();
    }
}
