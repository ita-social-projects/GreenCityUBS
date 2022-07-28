package greencity.mapping.location;

import greencity.dto.LocationsDtos;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class LocationToLocationsDtosMapper extends AbstractConverter<Location, LocationsDtos> {
    @Override
    public LocationsDtos convert(Location location) {
        return LocationsDtos.builder()
            .locationId(location.getId())
            .nameUk(location.getNameUk())
            .nameEn(location.getNameEn())
            .build();
    }
}
