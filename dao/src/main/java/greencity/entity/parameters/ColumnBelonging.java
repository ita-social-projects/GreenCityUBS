package greencity.entity.parameters;

import greencity.entity.language.Title;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnBelonging {
    private long id;
    private Title title;
    private List<Column> columnList;
}
