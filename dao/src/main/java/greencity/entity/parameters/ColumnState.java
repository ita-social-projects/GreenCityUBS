package greencity.entity.parameters;

import greencity.entity.enums.EditType;
import greencity.entity.language.Title;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ColumnState {
    private long id;
    private String name;
    private String titleForSorting;
    private int weight;
    private boolean sticky;
    private boolean visible;
    private int index;
    private EditType editType;
    private Title title;
    private TableParameters tableParameters;
    private ColumnBelonging columnBelonging;
}
