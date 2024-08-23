package greencity.mapping.useragreement;

import greencity.ModelUtils;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAgreementDtoMapperTest {
    @InjectMocks
    private UserAgreementDtoMapper mapper;

    @Test
    void shouldConvertUserAgreementToUserAgreementDto() {
        UserAgreement agreement = ModelUtils.getUserAgreement();
        UserAgreementDto expected = ModelUtils.getUserAgreementDto();

        UserAgreementDto actual = mapper.convert(agreement);

        Assertions.assertEquals(expected.getTextUa(), actual.getTextUa());
        Assertions.assertEquals(expected.getTextEn(), actual.getTextEn());
    }
}
