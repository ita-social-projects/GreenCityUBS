package greencity.entity.parameters;

import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@ToString
@Builder
public class TableParameters {
    private long id;
    private User user;
    private List<ColumnState> columnStateDTOList;
    private String sortingByColumn;
    private SortingOrder sortingOrder;
}
