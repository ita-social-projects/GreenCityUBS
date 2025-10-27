package greencity.dto.order;

import greencity.dto.RegionDto;
import greencity.dto.address.AddressDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.LocationsDto;
import greencity.dto.location.api.CityInfoDto;
import greencity.dto.location.api.DistrictDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderAddressDto {
    private Long id;
    private LocationsDto location;
    private CoordinatesDto coordinates;
    private RegionDto region;
    private CityInfoDto city;
    private DistrictDto district;
    private AddressDto baseAddress;
}
