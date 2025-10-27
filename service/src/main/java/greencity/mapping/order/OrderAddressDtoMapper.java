package greencity.mapping.order;

import greencity.dto.RegionDto;
import greencity.dto.address.AddressDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.api.CityInfoDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.DistrictInfoDto;
import greencity.dto.order.OrderAddressDto;
import greencity.entity.user.ubs.Address;
import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressDtoMapper extends AbstractConverter<Address, OrderAddressDto> {
    @Override
    protected OrderAddressDto convert(Address entity) {
        return OrderAddressDto.builder()
            .id(entity.getId())
            .baseAddress(AddressDto.builder()
                .regionEn(entity.getBaseAddress().getRegionEn())
                .cityEn(entity.getBaseAddress().getCityEn())
                .streetEn(entity.getBaseAddress().getStreetEn())
                .districtEn(entity.getBaseAddress().getDistrictEn())
                .regionUk(entity.getBaseAddress().getRegionUk())
                .cityUk(entity.getBaseAddress().getCityUk())
                .streetUk(entity.getBaseAddress().getStreetUk())
                .districtUk(entity.getBaseAddress().getDistrictUk())
                .houseNumber(entity.getBaseAddress().getHouseNumber())
                .houseCorpus(entity.getBaseAddress().getHouseCorpus())
                .entranceNumber(entity.getBaseAddress().getEntranceNumber())
                .addressComment(entity.getBaseAddress().getAddressComment())
                .actual(entity.getBaseAddress().getActual())
                .addressStatus(entity.getBaseAddress().getAddressStatus())
                .build())
            .city(CityInfoDto.builder()
                .id(entity.getCityId().getId())
                .regionId(entity.getCityId().getRegion().getId())
                .nameEn(entity.getCityId().getNameEn())
                .nameUk(entity.getCityId().getNameUk())
                .districts(entity.getCityId().getDistricts().stream()
                    .map(e -> DistrictInfoDto.builder()
                        .id(e.getId())
                        .cityId(e.getCity().getId())
                        .nameUk(e.getNameUk())
                        .nameEn(e.getNameEn())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .region(RegionDto.builder()
                .regionId(entity.getRegionId().getId())
                .nameEn(entity.getRegionId().getNameEn())
                .nameUk(entity.getRegionId().getNameUk())
                .build())
            .district(DistrictDto.builder()
                .id(entity.getDistrictId().getId())
                .nameEn(entity.getDistrictId().getNameEn())
                .nameUk(entity.getDistrictId().getNameUk())
                .build())
            .coordinates(CoordinatesDto.builder()
                .latitude(entity.getCoordinates().getLatitude())
                .longitude(entity.getCoordinates().getLongitude())
                .build())
            .build();
    }
}