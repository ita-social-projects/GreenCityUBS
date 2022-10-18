package greencity.dto.location;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class LocationToCityDto {
    private String cityName;
    private Long cityId;
    private CoordinatesDto coordinates;
}
