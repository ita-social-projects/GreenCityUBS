package greencity.dto.employee;

import greencity.dto.order.EmployeeOrderPositionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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