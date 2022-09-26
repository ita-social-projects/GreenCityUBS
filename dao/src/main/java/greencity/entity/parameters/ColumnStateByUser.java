package greencity.entity.parameters;

import greencity.dto.OptionForColumnDTO;
import greencity.entity.user.User;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnStateByUser {
    private int weight;
    private boolean sticky;
    private boolean visible;
    private int index;
    private User user;
    private Column column;
    private List<OptionForColumnDTO> optional;
}
