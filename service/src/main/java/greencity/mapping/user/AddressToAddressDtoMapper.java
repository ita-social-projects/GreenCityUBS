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
            .regionUk(address.getBaseAddress().getRegionUk())
            .regionEn(address.getBaseAddress().getRegionEn())
            .cityUk(address.getBaseAddress().getCityUk())
            .cityEn(address.getBaseAddress().getCityEn())
            .streetUk(address.getBaseAddress().getStreetUk())
            .streetEn(address.getBaseAddress().getStreetEn())
            .districtUk(address.getBaseAddress().getDistrictUk())
            .districtEn(address.getBaseAddress().getDistrictEn())
            .entranceNumber(address.getBaseAddress().getEntranceNumber())
            .houseCorpus(address.getBaseAddress().getHouseCorpus())
            .houseNumber(address.getBaseAddress().getHouseNumber())
            .addressComment(address.getBaseAddress().getAddressComment())
            .coordinates(Coordinates.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .addressRegionDistrictList(
                getAllDistricts((address.getBaseAddress().getRegionUk()), address.getBaseAddress().getCityUk()))
            .actual(address.getBaseAddress().getActual())
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
