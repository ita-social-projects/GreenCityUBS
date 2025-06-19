package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.User;
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

        user.setTelegramBot(null);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());

        AuthorizedUser telegramBot = ModelUtils.getTelegramBotNotifyFalse();
        user.setTelegramBot(telegramBot);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());

        user.setTelegramBot(null);
        userProfileUpdateDto.setTelegramIsNotify(false);
        converted = mapper.convert(user);
        assertEquals(userProfileUpdateDto.getTelegramIsNotify(), converted.getTelegramIsNotify());
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
