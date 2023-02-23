package greencity.dto.employee;

import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
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
public class EmployeeWithTariffsDto {
    private EmployeeDto employeeDto;
    @NotEmpty
    private List<GetTariffInfoForEmployeeDto> tariffs;
}
