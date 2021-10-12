package greencity.entity.parameters;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class OptionForColumn {
    private long id;
    private String ua;
    private String en;
    private boolean filtered;
}