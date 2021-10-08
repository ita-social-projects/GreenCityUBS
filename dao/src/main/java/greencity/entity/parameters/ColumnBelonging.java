package greencity.entity.parameters;

import greencity.entity.language.Title;
import lombok.*;

import java.util.List;

//@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ColumnBelonging {
    private long id;
    private Title title;
    private List<Column> columnList;
}
