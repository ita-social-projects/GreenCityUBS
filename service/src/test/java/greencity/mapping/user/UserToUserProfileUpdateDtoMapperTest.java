package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserToUserProfileUpdateDtoMapperTest {
    @InjectMocks
    private UserToUserProfileUpdateDtoMapper mapper;
    @Mock
    private LocationApiService locationApiService;

    @Test
    void convert() {

        MockitoAnnotations.initMocks(this);
        AddressDto expected = ModelUtils.getAddressDto(1L);
        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString()))
            .thenReturn(ModelUtils.getLocationApiDtoList());
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
