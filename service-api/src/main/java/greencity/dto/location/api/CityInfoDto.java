package greencity.dto.location.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString(exclude = {"districts"})
public class CityInfoDto extends BaseLocationsClass {
    private Long regionId;
    private List<DistrictInfoDto> districts;
}
