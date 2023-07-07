package greencity.dto.location.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistrictDto {
    private String nameUa;
    private String nameEn;
}
