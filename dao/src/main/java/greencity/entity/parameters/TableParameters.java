package greencity.entity.parameters;

import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;

//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@ToString
@Builder
public class TableParameters {
    //@Id
    private long id;
    //@Column(nullable = false)
    private User user;
    //@Column(nullable = false)
    private String sortingByColumn;
    //@Column(nullable = false)
    private SortingOrder sortingOrder;

    //@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tableParameters")
    private List<ColumnState> columnStateDTOList;
}
