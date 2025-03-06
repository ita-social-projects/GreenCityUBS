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
            .regionUk(address.getRegionUk())
            .regionEn(address.getRegionEn())
            .cityUk(address.getCityUk())
            .cityEn(address.getCityEn())
            .streetUk(address.getStreetUk())
            .streetEn(address.getStreetEn())
            .districtUk(address.getDistrictUk())
            .districtEn(address.getDistrictEn())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .houseNumber(address.getHouseNumber())
            .addressComment(address.getAddressComment())
            .coordinates(Coordinates.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .addressRegionDistrictList(getAllDistricts((address.getRegionUk()), address.getCityUk()))
            .actual(address.getActual())
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
