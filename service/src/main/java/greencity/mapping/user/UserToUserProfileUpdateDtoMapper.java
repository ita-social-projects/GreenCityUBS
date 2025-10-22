package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.user.UserProfileUpdateDto;
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
            .filter(address -> AddressStatus.DELETED != address.getBaseAddress().getAddressStatus())
            .map(this::createAddressDto)
            .toList();

        return UserProfileUpdateDto.builder()
            .recipientName(user.getRecipientName())
            .recipientSurname(user.getRecipientSurname())
            .recipientPhone(user.getRecipientPhone())
            .alternateEmail(user.getAlternateEmail())
            .addressDto(addressDtoList)
            .telegramIsNotify(user.getTelegramBot() != null && user.getTelegramBot().getIsNotify())
            .build();
    }

    private AddressDto createAddressDto(Address address) {
        return AddressDto.builder()
            .id(address.getId())
            .cityUk(address.getBaseAddress().getCityUk())
            .cityEn(address.getBaseAddress().getCityEn())
            .districtUk(address.getBaseAddress().getDistrictUk())
            .districtEn(address.getBaseAddress().getDistrictEn())
            .regionUk(address.getBaseAddress().getRegionUk())
            .regionEn(address.getBaseAddress().getRegionEn())
            .entranceNumber(address.getBaseAddress().getEntranceNumber())
            .houseCorpus(address.getBaseAddress().getHouseCorpus())
            .houseNumber(address.getBaseAddress().getHouseNumber())
            .streetUk(address.getBaseAddress().getStreetUk())
            .streetEn(address.getBaseAddress().getStreetEn())
            .addressComment(address.getBaseAddress().getAddressComment())
            .coordinates(CoordinatesDto.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .actual(address.getBaseAddress().getActual())
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
            .nameUk(district.getNameUk())
            .nameEn(district.getNameEn())
            .build();
    }
}
