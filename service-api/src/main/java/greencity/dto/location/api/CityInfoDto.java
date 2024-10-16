package greencity.dto.location.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"districts"})
@EqualsAndHashCode(callSuper = true)
public class CityInfoDto extends BaseLocationsClass {
    private Long regionId;
    private List<DistrictInfoDto> districts;
}
