package greencity.mapping.useragreement;

import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

/**
 * Class that is used by @link ModelMapper to map {@link UserAgreementDto} into
 * {@link UserAgreement}.
 */
@Component
public class UserAgreementMapper extends AbstractConverter<UserAgreementDto, UserAgreement> {
    /**
     * Method to convert {@link UserAgreementDto} to {@link UserAgreement}.
     *
     * @param userAgreementDto The {@link UserAgreementDto} to be converted.
     * @return {@link UserAgreement} entity representing the
     *         {@link UserAgreementDto}.
     */
    @Override
    protected UserAgreement convert(UserAgreementDto userAgreementDto) {
        return UserAgreement.builder()
            .textUa(userAgreementDto.getTextUa())
            .textEn(userAgreementDto.getTextEn())
            .build();
    }
}
