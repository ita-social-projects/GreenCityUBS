package greencity.mapping;

import greencity.dto.RegionDto;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class LocationToRegionDtoMapper extends AbstractConverter<Location, RegionDto> {

    @Override
    public RegionDto convert(Location sourse) {

        return RegionDto.builder()
                .regionId(sourse.getRegion().getId())
                .nameUk(sourse.getRegion().getUkrName())
                .nameEn(sourse.getRegion().getEnName())
                .build();
    }

}
