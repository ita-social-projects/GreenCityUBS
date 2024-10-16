package greencity.dto.employee;

import greencity.dto.tariff.TariffWithChatAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithTariffsIdDto {
    @Valid
    private EmployeeDto employeeDto;
    @NotEmpty
    private List<TariffWithChatAccess> tariffs;
}
