package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Address} into
 * {@link AddressDto}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddressToAddressDtoMapper extends AbstractConverter<Address, AddressDto> {
    private final DistrictRepository districtRepository;

    /**
     * Method convert {@link Address} to {@link AddressDto}.
     *
     * @return {@link AddressDto}
     */
    @Override
    public AddressDto convert(Address address) {
        log.info("Start mapping Address to DTO: ID = {}", address != null ? address.getId() : null);
        log.info(address.toString());
        try {
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
        } catch (Exception e) {
            log.error("Error in mapping Address (ID={}): {}", address.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private List<DistrictDto> getAllDistricts(String region, String city) {
        log.info("Fetching districts for region: {}, city: {}", region, city);
        //List<LocationDto> locationDtos = locationApiService.getAllDistrictsInCityByNames(region, city);
        List<District> locationDtos = districtRepository.findAllByCityId(1L);

        if (locationDtos == null) {
            log.warn("Received null from locationApiService");
            return List.of();
        }

        log.info(locationDtos.toString());
        //        return locationDtos.stream()
        //            .map(locationDto -> DistrictDto.builder()
        //                .nameUk(locationDto.getLocationNameMap().get("name_uk"))
        //                .nameEn(locationDto.getLocationNameMap().get("name_en"))
        //                .build())
        //            .collect(Collectors.toList());

        return locationDtos.stream()
            .map(locationDto -> DistrictDto.builder()
                .nameUk(locationDto.getNameUk())
                .nameEn(locationDto.getNameEn())
                .build())
            .collect(Collectors.toList());
    }
}
