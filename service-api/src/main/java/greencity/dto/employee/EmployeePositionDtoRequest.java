package greencity.dto.employee;

import greencity.dto.position.PositionDto;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class EmployeePositionDtoRequest {
    Long orderId;
    Map<PositionDto, List<EmployeeNameIdDto>> allPositionsEmployees;
    Map<PositionDto, String> currentPositionEmployees;
}