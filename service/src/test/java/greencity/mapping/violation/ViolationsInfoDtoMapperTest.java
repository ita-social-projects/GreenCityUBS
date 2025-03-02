package greencity.mapping.violation;

import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ViolationsInfoDtoMapperTest {

    @InjectMocks
    private ViolationsInfoDtoMapper mapper;

    @Test
    void convert() {
        ViolationsInfoDto violationsInfoDto = ViolationsInfoDto.builder().violationsAmount(1).build();
        User user = User.builder().violations(1).build();
        assertEquals(violationsInfoDto.getViolationsAmount(), mapper.convert(user).getViolationsAmount());

    }
}
