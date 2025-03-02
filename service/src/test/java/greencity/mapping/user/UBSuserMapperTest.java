package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UBSuserMapperTest {
    @InjectMocks
    UBSuserMapper ubsUserMapper;

    @Test
    void convert() {
        UBSuser expected = ModelUtils.getUBSuserWithoutOrderAddress();
        PersonalDataDto dto = ModelUtils.getOrderResponseDto().getPersonalData();
        dto.setId(1L);
        assertEquals(expected, ubsUserMapper.convert(dto));
    }
}
