package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.address.AddressWithDistrictsDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.service.locations.LocationApiService;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Address} into
 * {@link AddressWithDistrictsDto}.
 */
@Component
public class AddressToAddressWithDistrictsDtoMapper extends AbstractConverter<Address, AddressWithDistrictsDto> {
    /**
     * Service for getting districts in city.
     */
    @Autowired
    private LocationApiService locationApiService;

    /**
     * Method convert {@link Address} to {@link AddressWithDistrictsDto}.
     *
     * @return {@link AddressWithDistrictsDto}
     */
    @Override
    public AddressWithDistrictsDto convert(Address address) {
        return AddressWithDistrictsDto.builder().addressDto(AddressDto.builder()
            .id(address.getId())
            .region(address.getRegion())
            .regionEn(address.getRegionEn())
            .city(address.getCity())
            .cityEn(address.getCityEn())
            .street(address.getStreet())
            .streetEn(address.getStreetEn())
            .district(address.getDistrict())
            .districtEn(address.getDistrictEn())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .houseNumber(address.getHouseNumber())
            .addressComment(address.getAddressComment())
            .coordinates(Coordinates.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .actual(address.getActual()).build())
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
