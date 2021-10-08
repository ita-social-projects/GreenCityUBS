package greencity.entity.parameters;

import greencity.dto.OptionForColumnDTO;
import greencity.entity.user.User;
import lombok.*;

import java.util.List;

//@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ColumnStateByUser {
    private int weight;
    private boolean sticky;
    private boolean visible;
    private int index;
    //@ManyToOne
    private User user;
    //@OneToMany
    private Column column;
    //@OneToMany
    private List<OptionForColumnDTO> optional;
}
