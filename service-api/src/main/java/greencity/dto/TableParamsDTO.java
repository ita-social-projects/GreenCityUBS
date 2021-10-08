package greencity.dto;

import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class TableParamsDTO {
    private OrderPage page;
    private OrderSearchCriteria orderSearchCriteria;
    private List<ColumnDTO> columnDTOList;
    private List<TitleDto> columnBelongingList;
}
