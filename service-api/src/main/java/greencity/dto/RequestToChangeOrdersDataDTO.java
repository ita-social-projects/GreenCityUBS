package greencity.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class RequestToChangeOrdersDataDTO {
    private List<Long> orderId;
    private String columnName;
    private String newValue;
}
