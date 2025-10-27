package greencity.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionForColumnDTO {
    private String key;
    private String uk;
    private String en;
    private boolean filtered;
}