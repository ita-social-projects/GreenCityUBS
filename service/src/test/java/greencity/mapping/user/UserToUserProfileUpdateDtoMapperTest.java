package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserToUserProfileUpdateDtoMapperTest {
    @InjectMocks
    private UserToUserProfileUpdateDtoMapper mapper;

    @Test
    void convert() {
        UserProfileUpdateDto userProfileUpdateDto = ModelUtils.updateUserProfileDto();
        User user = ModelUtils.getUserWithBot();
        UserProfileUpdateDto converted = mapper.convert(user);
        Assertions.assertEquals(userProfileUpdateDto.getRecipientName(), converted.getRecipientName());
        Assertions.assertEquals(userProfileUpdateDto.getRecipientSurname(), converted.getRecipientSurname());
        Assertions.assertEquals(userProfileUpdateDto.getRecipientPhone(), converted.getRecipientPhone());
        Assertions.assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        Assertions.assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());
    }
}
