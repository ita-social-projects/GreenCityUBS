package greencity.dto.location.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"cities"})
@EqualsAndHashCode(callSuper = true)
public class RegionInfoDto extends BaseLocationsClass {
    private List<CityInfoDto> cities;
}
