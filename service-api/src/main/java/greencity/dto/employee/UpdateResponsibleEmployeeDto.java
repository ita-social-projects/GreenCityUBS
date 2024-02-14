package greencity.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateResponsibleEmployeeDto {
    private Long positionId;
    private Long employeeId;
}
