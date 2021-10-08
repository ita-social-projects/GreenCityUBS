package greencity.entity.parameters;

import greencity.dto.TitleDto;
import greencity.entity.enums.EditType;
import greencity.entity.language.Title;
import greencity.entity.user.User;
import lombok.*;

import javax.persistence.OneToOne;
import java.util.List;

//@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class Column {
    private long id;
    private String name;
    private String titleForSorting;
    private boolean filtered;
    private EditType editType;

    //@OneToOne
    private Title title;
    //@ManyToOne
    private TableParameters tableParameters;
    //@ManyToOne
    private ColumnBelonging columnBelonging;
    //@ManyToMany
    private List<User> abilityToChange;
    //@OneToOne
    private ColumnStateByUser columnStateByUser;

}
