package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.service.locations.LocationApiService;
import org.modelmapper.AbstractConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserToUserProfileUpdateDtoMapper extends AbstractConverter<User, UserProfileUpdateDto> {
    /**
     * Service for getting districts in city.
     */
    @Autowired
    private LocationApiService locationApiService;

    /**
     * Method convert {@link User} to {@link UserProfileUpdateDto}.
     *
     * @return {@link UserProfileUpdateDto}
     */
    @Override
    protected UserProfileUpdateDto convert(User user) {
        List<AddressDto> addressDtoList = user.getAddresses().stream()
            .filter(Address::getActual)
            .map(this::createAddressDto)
            .collect(Collectors.toList());

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
            .addressRegionDistrictList(getAllDistricts((address.getRegion()), address.getCity()))
            .build();
    }

    private List<DistrictDto> getAllDistricts(String region, String city) {
        List<LocationDto> locationDtos = locationApiService.getAllDistrictsInCityByNames(region, city);
        return locationDtos.stream()
            .map(locationDto -> DistrictDto.builder()
                .nameUa(locationDto.getLocationNameMap().get("name"))
                .nameEn(locationDto.getLocationNameMap().get("name_en"))
                .build())
            .collect(Collectors.toList());
    }
}
