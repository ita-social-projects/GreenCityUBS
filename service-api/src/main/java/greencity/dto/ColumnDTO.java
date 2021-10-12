package greencity.dto;

import greencity.entity.enums.EditType;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ColumnDTO {
    private TitleDto title;
    private String titleForSorting;
    private int weight;
    private boolean sticky;
    private boolean visible;
    private boolean filtered;
    private int index;
    private EditType editType;
    private List<OptionForColumnDTO> optional;
    private String columnBelonging;
}
