package greencity.dto;

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
    Map<PositionDto, List<String>> allPositionsEmployees;
    Map<PositionDto, String> currentPositionEmployees;
}