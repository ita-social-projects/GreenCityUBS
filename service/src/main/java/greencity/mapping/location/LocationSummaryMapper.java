package greencity.mapping.location;

import greencity.dto.location.*;
import greencity.entity.user.Region;
import greencity.enums.LocationStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationSummaryMapper extends AbstractConverter<Region, LocationSummaryDto> {
    @Override
    protected LocationSummaryDto convert(Region source) {
        List<LocationToCityDto> citiesUaDtoList = source.getLocations().stream()
                .filter(location -> location.getLocationStatus() == LocationStatus.ACTIVE)
                .map(location -> LocationToCityDto.builder()
                        .cityName(location.getNameUk())
                        .cityId(location.getId())
                        .coordinates(CoordinatesDto.builder().latitude(location.getCoordinates().getLatitude()).longitude(location.getCoordinates().getLongitude()).build())
                        .build()).collect(Collectors.toList());

        List<LocationToCityDto> citiesEnDtoList = source.getLocations().stream()
                .filter(location -> location.getLocationStatus() == LocationStatus.ACTIVE)
                .map(location -> LocationToCityDto.builder()
                        .cityName(location.getNameEn())
                        .cityId(location.getId())
                        .coordinates(CoordinatesDto.builder().latitude(location.getCoordinates().getLatitude()).longitude(location.getCoordinates().getLongitude()).build())
                .build())
                .collect(Collectors.toList());

        return LocationSummaryDto.builder()
                .regionId(source.getId())
                .nameEn(source.getEnName())
                .nameUa(source.getUkrName())
                .citiesUa(citiesUaDtoList)
                .citiesEn(citiesEnDtoList)
                .build();
    }
}
