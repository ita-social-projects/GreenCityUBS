package greencity.dto.position;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PositionAuthoritiesDto {
    List<Long> positionId;
    List<String> authorities;
}
