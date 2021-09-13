package greencity.entity.parameters;

import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;

import java.util.List;

public class TableParameters {
    private long id;
    private User user;
    private String sortingByColumn;
    private SortingOrder sortingOrder;
    private List<ColumnState> columnStateDTOList;
}
