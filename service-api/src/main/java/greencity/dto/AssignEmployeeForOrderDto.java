package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AssignEmployeeForOrderDto {
    private Long positionId;
    private Long employeeId;
}
