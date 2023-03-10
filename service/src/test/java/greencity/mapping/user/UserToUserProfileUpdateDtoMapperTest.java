package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
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
        User user = ModelUtils.getUserWithBotNotifyTrue();
        UserProfileUpdateDto converted = mapper.convert(user);
        Assertions.assertEquals(userProfileUpdateDto.getRecipientName(), converted.getRecipientName());
        Assertions.assertEquals(userProfileUpdateDto.getRecipientSurname(), converted.getRecipientSurname());
        Assertions.assertEquals(userProfileUpdateDto.getRecipientPhone(), converted.getRecipientPhone());
        Assertions.assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        Assertions.assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());

        ViberBot viberBot = ModelUtils.getViberBotNotifyTrue();
        user.setViberBot(viberBot);
        user.setTelegramBot(null);
        userProfileUpdateDto.setViberIsNotify(true);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        Assertions.assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        Assertions.assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());

        viberBot = ModelUtils.getViberBotNotifyFalse();
        TelegramBot telegramBot = ModelUtils.getTelegramBotNotifyFalse();
        user.setViberBot(viberBot);
        user.setTelegramBot(telegramBot);
        userProfileUpdateDto.setViberIsNotify(false);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        Assertions.assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        Assertions.assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());

        user.setViberBot(null);
        user.setTelegramBot(null);
        userProfileUpdateDto.setViberIsNotify(false);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        Assertions.assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        Assertions.assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());
    }
}
