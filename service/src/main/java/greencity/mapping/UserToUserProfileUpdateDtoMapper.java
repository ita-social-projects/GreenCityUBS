package greencity.mapping;

import greencity.dto.AddressDto;
import greencity.dto.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.User;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserToUserProfileUpdateDtoMapper extends AbstractConverter<User, UserProfileUpdateDto> {
    @Override
    protected UserProfileUpdateDto convert(User user) {
        List<AddressDto> addressDtoList = user.getAddresses().stream()
            .map(address -> AddressDto.builder()
                .id(address.getId())
                .city(address.getCity())
                .district(address.getDistrict())
                .region(address.getRegion())
                .entranceNumber(address.getEntranceNumber())
                .houseCorpus(address.getHouseCorpus())
                .houseNumber(address.getHouseNumber())
                .street(address.getStreet())
                .addressComment(address.getAddressComment())
                .coordinates(Coordinates.builder()
                    .latitude(address.getCoordinates().getLatitude())
                    .longitude(address.getCoordinates().getLongitude())
                    .build())
                .actual(address.getActual())
                .build())
            .collect(Collectors.toList());

        return UserProfileUpdateDto.builder()
            .recipientName(user.getRecipientName())
            .recipientSurname(user.getRecipientSurname())
            .recipientPhone(user.getRecipientPhone())
            .addressDto(addressDtoList)
            .build();
    }
}
