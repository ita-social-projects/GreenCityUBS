package greencity.dto;

import greencity.entity.enums.SortingOrder;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class TableParamsDTO {
    private List<ColumnStateDTO> columnStateDTOList;
    private String sortingByColumn;
    private SortingOrder sortingOrder;
    private List<TitleDto> columnBelongingList;
}
