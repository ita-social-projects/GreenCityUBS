package greencity.mapping.useragreement;

import greencity.dto.user.UserAgreementDetailDto;
import greencity.entity.user.UserAgreement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserAgreementDtoDetailMapper extends AbstractConverter<UserAgreement, UserAgreementDetailDto> {
    @Override
    protected UserAgreementDetailDto convert(UserAgreement userAgreement) {
        return UserAgreementDetailDto.builder()
                .id(userAgreement.getId())
                .textUa(userAgreement.getTextUa())
                .textEn(userAgreement.getTextEn())
                .createdAt(userAgreement.getCreatedAt())
                .updatedAt(userAgreement.getUpdatedAt())
                .build();
    }
}
