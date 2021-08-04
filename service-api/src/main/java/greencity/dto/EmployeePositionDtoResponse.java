package greencity.dto;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EmployeePositionDtoResponse implements Serializable {
    private Long orderId;
    private List<EmployeeOrderPositionDTO> employeeOrderPositionDTOS;
}