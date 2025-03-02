package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class RequestToChangeOrdersDataDto {
    private List<Long> orderIdsList;
    private String columnName;
    private String newValue;
}
