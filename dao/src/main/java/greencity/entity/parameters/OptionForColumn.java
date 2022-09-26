package greencity.entity.parameters;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionForColumn {
    private long id;
    private String ua;
    private String en;
    private boolean filtered;
}