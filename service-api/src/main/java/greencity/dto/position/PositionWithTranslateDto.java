package greencity.dto.position;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class PositionWithTranslateDto {
    @Min(1)
    private Long id;

    private Map<String, String> name;
}
