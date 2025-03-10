package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.service.locations.LocationApiService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Address} into
 * {@link AddressDto}.
 */
@Component
@RequiredArgsConstructor
public class AddressToAddressDtoMapper extends AbstractConverter<Address, AddressDto> {
    private final LocationApiService locationApiService;

    /**
     * Method convert {@link Address} to {@link AddressDto}.
     *
     * @return {@link AddressDto}
     */
    @Override
    public AddressDto convert(Address address) {
        return AddressDto.builder()
            .id(address.getId())
            .regionUk(address.getAddress().getRegionUk())
            .regionEn(address.getAddress().getRegionEn())
            .cityUk(address.getAddress().getCityUk())
            .cityEn(address.getAddress().getCityEn())
            .streetUk(address.getAddress().getStreetUk())
            .streetEn(address.getAddress().getStreetEn())
            .districtUk(address.getAddress().getDistrictUk())
            .districtEn(address.getAddress().getDistrictEn())
            .entranceNumber(address.getAddress().getEntranceNumber())
            .houseCorpus(address.getAddress().getHouseCorpus())
            .houseNumber(address.getAddress().getHouseNumber())
            .addressComment(address.getAddress().getAddressComment())
            .coordinates(Coordinates.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .addressRegionDistrictList(
                getAllDistricts((address.getAddress().getRegionUk()), address.getAddress().getCityUk()))
            .actual(address.getAddress().getActual())
            .build();
    }

    private List<DistrictDto> getAllDistricts(String region, String city) {
        List<LocationDto> locationDtos = locationApiService.getAllDistrictsInCityByNames(region, city);
        return locationDtos.stream()
            .map(locationDto -> DistrictDto.builder()
                .nameUk(locationDto.getLocationNameMap().get("name"))
                .nameEn(locationDto.getLocationNameMap().get("name_en"))
                .build())
            .collect(Collectors.toList());
    }
}
