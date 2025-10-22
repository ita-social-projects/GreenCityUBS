package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.api.DistrictDto;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.repository.CityRepository;
import greencity.repository.DistrictRepository;
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
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;

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
            .coordinates(CoordinatesDto.builder()
                .latitude(address.getCoordinates().getLatitude())
                .longitude(address.getCoordinates().getLongitude())
                .build())
            .addressRegionDistrictList(
                getAllDistricts(address.getBaseAddress().getCityUk()))
            .actual(address.getBaseAddress().getActual())
            .build();
    }

    private List<DistrictDto> getAllDistricts(String city) {
        List<District> districtDtos = districtRepository.findAllByCityId(cityRepository.findIdByNameUkOrNameEn(city));

        if (districtDtos == null) {
            return List.of();
        }

        return districtDtos.stream()
            .map(districtDto -> DistrictDto.builder()
                .nameUk(districtDto.getNameUk())
                .nameEn(districtDto.getNameEn())
                .build())
            .collect(Collectors.toList());
    }
}
