package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class OptionForColumnDTO {
    private String key;
    private String ua;
    private String en;
    private boolean filtered;
}