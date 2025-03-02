package greencity.mapping.useragreement;

import greencity.ModelUtils;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.entity.user.UserAgreement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAgreementDetailDtoMapperTest {
    @InjectMocks
    private UserAgreementDetailDtoMapper mapper;

    @Test
    void shouldConvertUserAgreementToUserAgreementDetailDto() {
        UserAgreement agreement = ModelUtils.getUserAgreement();
        UserAgreementDetailDto expected = ModelUtils.getUserAgreementDetailDto();

        UserAgreementDetailDto actual = mapper.convert(agreement);

        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getTextUa(), actual.getTextUa());
        Assertions.assertEquals(expected.getTextEn(), actual.getTextEn());
        Assertions.assertEquals(expected.getAuthorEmail(), actual.getAuthorEmail());
        Assertions.assertNotNull(expected.getCreatedAt());
    }
}
