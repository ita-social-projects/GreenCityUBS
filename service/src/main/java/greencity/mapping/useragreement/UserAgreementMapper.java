package greencity.mapping.useragreement;

import greencity.dto.user.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserAgreementMapper extends AbstractConverter<UserAgreementDto, UserAgreement> {
    @Override
    protected UserAgreement convert(UserAgreementDto userAgreementDto) {
        return UserAgreement.builder()
                .textUa(userAgreementDto.getTextUa())
                .textEn(userAgreementDto.getTextEn())
                .build();
    }
}
