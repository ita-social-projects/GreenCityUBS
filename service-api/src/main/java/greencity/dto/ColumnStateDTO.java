package greencity.dto;

import greencity.entity.enums.EditType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ColumnStateDTO {
    private String name;
    private TitleDto title;
    private int weight;
    private boolean sticky;
    private boolean visible;
    private int index;
    private EditType editType;
}
