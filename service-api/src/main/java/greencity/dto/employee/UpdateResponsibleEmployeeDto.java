package greencity.dto.employee;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateResponsibleEmployeeDto {
    private Long positionId;
    private Long employeeId;
}
