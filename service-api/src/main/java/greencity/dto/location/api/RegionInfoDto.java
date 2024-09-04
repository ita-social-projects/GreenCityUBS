package greencity.dto.location.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString(exclude = {"cities"})
public class RegionInfoDto extends BaseLocationsClass {
    private List<CityInfoDto> cities;
}
