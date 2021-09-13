package greencity.entity.parameters;

import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class TableParameters {
    private long id;
    private User user;
    private String sortingByColumn;
    private SortingOrder sortingOrder;
    private List<ColumnState> columnStateDTOList;
}
