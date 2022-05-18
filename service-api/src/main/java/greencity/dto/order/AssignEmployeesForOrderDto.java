package greencity.dto.order;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AssignEmployeesForOrderDto {
    private Long orderId;
    List<AssignForOrderEmployee> employeesList;
}
