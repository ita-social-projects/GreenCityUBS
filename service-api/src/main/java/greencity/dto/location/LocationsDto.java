package greencity.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class LocationsDto {
    private Long locationId;
    private String locationStatus;
    private Double latitude;
    private Double longitude;
    private List<LocationTranslationDto> locationTranslationDtoList;
}
