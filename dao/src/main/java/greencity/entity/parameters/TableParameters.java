package greencity.entity.parameters;

import greencity.entity.user.User;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableParameters {
    private long id;
    private User user;
    private OrderPage page;
    private OrderSearchCriteria orderSearchCriteria;
    private List<Column> columnList;
    private List<ColumnBelonging> columnBelongingList;
}
