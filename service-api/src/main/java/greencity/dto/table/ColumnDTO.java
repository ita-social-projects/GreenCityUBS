package greencity.dto.table;

import greencity.dto.OptionForColumnDTO;
import greencity.dto.TitleDto;
import greencity.enums.EditType;
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
    private List<OptionForColumnDTO> checked;
    private String columnBelonging;
}
