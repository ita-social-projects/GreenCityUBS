package greencity.dto.order;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class RequestToChangeOrdersDataDto {
    private List<Long> orderId;
    private String columnName;
    private String newValue;
}
