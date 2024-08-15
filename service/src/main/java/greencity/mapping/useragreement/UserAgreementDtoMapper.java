package greencity.mapping.useragreement;

import greencity.dto.user.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserAgreementDtoMapper extends AbstractConverter<UserAgreement, UserAgreementDto> {
    @Override
    protected UserAgreementDto convert(UserAgreement userAgreement) {
        return UserAgreementDto.builder()
                .textUa(userAgreement.getTextUa())
                .textEn(userAgreement.getTextEn())
                .build();
    }
}
