package greencity.mapping.user;

import greencity.dto.user.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link PersonalDataDto} into
 * {@link UBSuser}.
 */
@Component
public class UBSuserMapper extends AbstractConverter<PersonalDataDto, UBSuser> {
    /**
     * Method convert {@link PersonalDataDto} to {@link UBSuser}.
     *
     * @return {@link UBSuser}
     */
    @Override
    protected UBSuser convert(PersonalDataDto personalDataDto) {
        return UBSuser.builder()
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
