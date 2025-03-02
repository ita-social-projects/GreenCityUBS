package greencity.dto.location.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class BaseLocationsClass {
    private Long id;
    private String nameEn;
    private String nameUk;
}
