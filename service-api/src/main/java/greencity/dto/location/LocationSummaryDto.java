package greencity.dto.location;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class LocationSummaryDto {
    private Long regionId;
    private String nameEn;
    private String nameUa;
    private List<LocationToCityDto> citiesUa;
    private List<LocationToCityDto> citiesEn;
}
