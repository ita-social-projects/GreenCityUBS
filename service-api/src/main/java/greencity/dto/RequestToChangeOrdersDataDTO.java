package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class RequestToChangeOrdersDataDTO {
    private String columnName;
    private Object newValue;
}
