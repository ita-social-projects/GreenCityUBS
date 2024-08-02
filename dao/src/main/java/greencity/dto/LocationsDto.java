package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationsDto {
    private Long id;
    private String locationStatus;
    private String regionNameUk;
    private String regionNameEn;
    private Double latitude;
    private Double longitude;
    private String nameUk;
    private String nameEn;
    private Long tariffsId;
}