package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UBSuserMapperTest {
    @InjectMocks
    UBSuserMapper ubsUserMapper;

    @Test
    void convert() {
        UBSuser expected = ModelUtils.getUBSuser();
        PersonalDataDto dto = ModelUtils.getOrderResponseDto().getPersonalData();
        dto.setId(1L);

        assertEquals(expected, ubsUserMapper.convert(dto));
    }
}
