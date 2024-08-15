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
public class UserAgreementMapperTest {
    @InjectMocks
    private UserAgreementMapper mapper;

    @Test
    void shouldConvertUserAgreementDtoToUserAgreement() {
        UserAgreementDto dto = ModelUtils.getUserAgreementDto();
        UserAgreement expected = ModelUtils.getUserAgreement();

        UserAgreement actual = mapper.convert(dto);

        Assertions.assertEquals(expected.getTextUa(), actual.getTextUa());
        Assertions.assertEquals(expected.getTextEn(), actual.getTextEn());
        Assertions.assertNull(actual.getCreatedAt());
        Assertions.assertNull(actual.getUpdatedAt());
    }
}
