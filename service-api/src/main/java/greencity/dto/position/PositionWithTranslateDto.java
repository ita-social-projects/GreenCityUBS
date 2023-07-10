package greencity.dto.position;

import lombok.Data;
import lombok.Builder;
import javax.validation.constraints.Min;
import java.util.Map;

@Builder
@Data
public class PositionWithTranslateDto {
    @Min(1)
    private Long id;

    private Map<String, String> name;
}
