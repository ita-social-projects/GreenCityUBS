package greencity.mapping.user;

import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.service.locations.LocationApiService;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddressToAddressDtoMapper extends AbstractConverter<Address, AddressDto> {
    LocationApiService locationApiService;

    public AddressToAddressDtoMapper(LocationApiService locationApiService) {
        this.locationApiService = locationApiService;
    }

    public static String deleteLastWord(String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return "";
        }

        String[] words = sentence.split("\\s+");
        if (words.length <= 1) {
            return "";
        }

        return Arrays.stream(words, 0, words.length - 1)
                .reduce((word1, word2) -> word1 + " " + word2)
                .orElse("");
    }

    @Override
    protected AddressDto convert(Address address) {
        return AddressDto.builder()
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
                .build()).addressRegionDistrictList(getAllDistricts(deleteLastWord(address.getRegion()),address.getCity()))
            .actual(false)
            .build();
    }

    public List<DistrictDto> getAllDistricts(String region, String city) {
        List<LocationDto> locationDtos = locationApiService.getAllDistrictsInCityByNames(region, city);
        return locationDtos.stream()
                .map(locationDto-> DistrictDto.builder()
                        .nameUa(locationDto.getLocationNameMap().get("name"))
                        .nameEn(locationDto.getLocationNameMap().get("name_en"))
                        .build())
                .collect(Collectors.toList());
    }

}
