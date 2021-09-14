package greencity.dto;

import greencity.entity.enums.DataColumnType;
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
    private List<Object> newValues;
    private DataColumnType dataColumnType;
}
