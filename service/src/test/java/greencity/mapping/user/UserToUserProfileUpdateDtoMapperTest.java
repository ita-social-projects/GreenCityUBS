package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import greencity.repository.DistrictRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserToUserProfileUpdateDtoMapperTest {
    @InjectMocks
    private UserToUserProfileUpdateDtoMapper mapper;
    @Mock
    private DistrictRepository districtRepository;

    @Test
    void convert() {
        when(districtRepository.findAllByCityId(anyLong()))
            .thenReturn(List.of(ModelUtils.getDistrict()));
        UserProfileUpdateDto userProfileUpdateDto = ModelUtils.updateUserProfileDto();
        User user = ModelUtils.getUserWithBotNotifyTrue_AddressTrue();
        UserProfileUpdateDto converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getRecipientName(), converted.getRecipientName());
        assertEquals(userProfileUpdateDto.getRecipientSurname(), converted.getRecipientSurname());
        assertEquals(userProfileUpdateDto.getRecipientPhone(), converted.getRecipientPhone());
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());

        ViberBot viberBot = ModelUtils.getViberBotNotifyTrue();
        user.setViberBot(viberBot);
        user.setTelegramBot(null);
        userProfileUpdateDto.setViberIsNotify(true);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());

        viberBot = ModelUtils.getViberBotNotifyFalse();
        AuthorizedUser telegramBot = ModelUtils.getTelegramBotNotifyFalse();
        user.setViberBot(viberBot);
        user.setTelegramBot(telegramBot);
        userProfileUpdateDto.setViberIsNotify(false);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());

        user.setViberBot(null);
        user.setTelegramBot(null);
        userProfileUpdateDto.setViberIsNotify(false);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
        assertEquals(userProfileUpdateDto.getViberIsNotify(), converted.getViberIsNotify());
    }

    @Test
    void convertWithEmptyAddressTest() {
        assertEquals(
            Collections.emptyList(),
            mapper.convert(ModelUtils.getUser())
                .getAddressDto()
                .getFirst()
                .getAddressRegionDistrictList());
    }
}
