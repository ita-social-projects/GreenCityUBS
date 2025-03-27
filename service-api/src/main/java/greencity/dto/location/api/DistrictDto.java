package greencity.dto.location.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDto implements Serializable {
    private String nameUa;
    private String nameEn;
}
