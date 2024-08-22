package greencity.mapping.useragreement;

import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.entity.user.UserAgreement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

/**
 * Class that is used by ModelMapper to map {@link UserAgreement} into
 * {@link UserAgreementDetailDto}.
 */
@Component
public class UserAgreementDetailDtoMapper extends AbstractConverter<UserAgreement, UserAgreementDetailDto> {
    /**
     * Method to convert {@link UserAgreement} to {@link UserAgreementDetailDto}.
     *
     * @param userAgreement The {@link UserAgreement} entity to be converted.
     * @return {@link UserAgreementDetailDto} representing the {@link UserAgreement}
     *         entity.
     */

    @Override
    protected UserAgreementDetailDto convert(UserAgreement userAgreement) {
        return UserAgreementDetailDto.builder()
            .id(userAgreement.getId())
            .textUa(userAgreement.getTextUa())
            .textEn(userAgreement.getTextEn())
            .createdAt(userAgreement.getCreatedAt())
            .authorEmail(userAgreement.getAuthor().getEmail())
            .build();
    }
}
