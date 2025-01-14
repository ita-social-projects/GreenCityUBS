package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.User;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.enums.AddressStatus;
import greencity.repository.DistrictRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserToUserProfileUpdateDtoMapper extends AbstractConverter<User, UserProfileUpdateDto> {
    private final DistrictRepository districtRepository;

    /**
     * Method convert {@link User} to {@link UserProfileUpdateDto}.
     *
     * @return {@link UserProfileUpdateDto}
     */
    @Override
    protected UserProfileUpdateDto convert(User user) {
        List<AddressDto> addressDtoList = user.getAddresses().stream()
            .filter(address -> AddressStatus.DELETED != address.getAddressStatus())
            .map(this::createAddressDto)
            .toList();

        return UserProfileUpdateDto.builder()
            .recipientName(user.getRecipientName())
            .recipientSurname(user.getRecipientSurname())
            .recipientPhone(user.getRecipientPhone())
            .alternateEmail(user.getAlternateEmail())
            .addressDto(addressDtoList)
            .telegramIsNotify(user.getTelegramBot() != null && user.getTelegramBot().getIsNotify())
            .viberIsNotify(user.getViberBot() != null && user.getViberBot().getIsNotify())
            .build();
    }

    private AddressDto createAddressDto(Address address) {
        return AddressDto.builder()
            .id(address.getId())
            .city(address.getCity())
            .cityEn(address.getCityEn())
            .district(address.getDistrict())
            .districtEn(address.getDistrictEn())
            .region(address.getRegion())
            .regionEn(address.getRegionEn())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .houseNumber(address.getHouseNumber())
            .street(address.getStreet())
            .streetEn(address.getStreetEn())
            .addressComment(address.getAddressComment())
            .coordinates(Coordinates.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .actual(address.getActual())
            .addressRegionDistrictList(getAllDistricts(address))
            .build();
    }

    private List<DistrictDto> getAllDistricts(Address address) {
        if (address == null || address.getCityId() == null) {
            return List.of();
        }
        return districtRepository.findAllByCityId(address.getCityId().getId()).stream()
            .map(this::getDistrictDto)
            .toList();
    }

    private DistrictDto getDistrictDto(District district) {
        return DistrictDto.builder()
            .nameUa(district.getNameUk())
            .nameEn(district.getNameEn())
            .build();
    }
}
