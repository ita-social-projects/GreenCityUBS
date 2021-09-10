package greencity.entity.parameters;

import greencity.entity.enums.EditType;
import greencity.entity.language.Title;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@ToString
@Builder
public class ColumnState {
    private long id;
    private String name;
    private Title title;
    private int weight;
    private boolean sticky;
    private boolean visible;
    private int index;
    private EditType editType;
}
