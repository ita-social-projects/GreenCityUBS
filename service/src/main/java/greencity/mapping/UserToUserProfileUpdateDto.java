package greencity.mapping;

import greencity.dto.AddressDto;
import greencity.dto.UserProfileUpdateDto;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserToUserProfileUpdateDto extends AbstractConverter<User, UserProfileUpdateDto> {
    @Override
    protected UserProfileUpdateDto convert(User user) {
        List<Address> addresses = user.getAddresses();
        List<AddressDto> addressDtoList = addresses.stream()
            .map(address -> new AddressDto(address.getId(), address.getCity(), address.getDistrict(),
                address.getRegion(), address.getEntranceNumber(), address.getHouseCorpus(), address.getHouseNumber(),
                address.getStreet(), address.getAddressComment(), address.getCoordinates(), address.getActual()))
            .collect(Collectors.toList());

        return UserProfileUpdateDto.builder()
            .recipientName(user.getRecipientName())
            .recipientSurname(user.getRecipientSurname())
            .recipientPhone(user.getRecipientPhone())
            .addressDto(addressDtoList)
            .build();
    }
}
