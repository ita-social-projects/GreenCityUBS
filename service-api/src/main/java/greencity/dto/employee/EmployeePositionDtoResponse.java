package greencity.dto.employee;

import greencity.dto.order.EmployeeOrderPositionDTO;
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