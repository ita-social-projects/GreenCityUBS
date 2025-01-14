package greencity.dto.table;

import greencity.dto.OptionForColumnDTO;
import greencity.dto.TitleDto;
import greencity.enums.EditType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
