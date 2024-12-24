package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileDto;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserToUserProfileDtoMapperTest {

    @InjectMocks
    private UserToUserProfileDtoMapper mapper;

    @Test
    void convert() {
        UserProfileDto expected = ModelUtils.userProfileDto();
        User user = ModelUtils.getUserProfile();
        ViberBot viberBot = ModelUtils.getViberBotNotifyTrue();

        assertEquals(expected, mapper.convert(user));

        user.setViberBot(viberBot);
        user.setTelegramBot(null);
        expected.setViberIsNotify(true);
        expected.setTelegramIsNotify(false);
        assertEquals(expected, mapper.convert(user));

        TelegramBot telegramBot = ModelUtils.getTelegramBotNotifyFalse();
        viberBot = ModelUtils.getViberBotNotifyFalse();
        user.setViberBot(viberBot);
        user.setTelegramBot(telegramBot);
        expected.setViberIsNotify(false);
        expected.setTelegramIsNotify(false);
        assertEquals(expected, mapper.convert(user));

        user.setViberBot(null);
        user.setTelegramBot(null);
        expected.setViberIsNotify(false);
        expected.setTelegramIsNotify(false);
        assertEquals(expected, mapper.convert(user));
    }
}
