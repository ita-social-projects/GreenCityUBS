package greencity.entity.parameters;

import greencity.entity.enums.EditType;
import greencity.entity.language.Title;
import greencity.entity.user.User;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Column {
    private long id;
    private String key;
    private String titleForSorting;
    private boolean filtered;
    private EditType editType;
    private Title title;
    private TableParameters tableParameters;
    private ColumnBelonging columnBelonging;
    private List<User> abilityToChange;
    private ColumnStateByUser columnStateByUser;
}
