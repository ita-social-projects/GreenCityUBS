package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.user.User;
import greencity.mapping.user.UserToUserProfileUpdateDtoMapper;
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
        User user = ModelUtils.getUser();
        Assertions.assertEquals(userProfileUpdateDto.getRecipientName(), mapper.convert(user).getRecipientName());
        Assertions.assertEquals(userProfileUpdateDto.getRecipientSurname(), mapper.convert(user).getRecipientSurname());
        Assertions.assertEquals(userProfileUpdateDto.getRecipientPhone(), mapper.convert(user).getRecipientPhone());
    }
}
