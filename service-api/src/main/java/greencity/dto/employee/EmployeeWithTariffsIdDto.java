package greencity.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithTariffsIdDto {
    private EmployeeDto employeeDto;
    @NotEmpty
    private List<Long> tariffId;
}
