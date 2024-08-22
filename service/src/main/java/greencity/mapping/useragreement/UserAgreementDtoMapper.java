package greencity.mapping.useragreement;

import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

/**
 * Class that is used by @link ModelMapper to map {@link UserAgreement} into
 * {@link UserAgreementDto}.
 */
@Component
public class UserAgreementDtoMapper extends AbstractConverter<UserAgreement, UserAgreementDto> {
    /**
     * Method to convert {@link UserAgreement} to {@link UserAgreementDto}.
     *
     * @param userAgreement The {@link UserAgreement} entity to be converted.
     * @return {@link UserAgreementDto} representing the {@link UserAgreement}
     *         entity.
     */
    @Override
    protected UserAgreementDto convert(UserAgreement userAgreement) {
        return UserAgreementDto.builder()
            .textUa(userAgreement.getTextUa())
            .textEn(userAgreement.getTextEn())
            .build();
    }
}
