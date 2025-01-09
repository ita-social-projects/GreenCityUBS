package greencity.dto.employee;

import greencity.dto.tariff.TariffWithChatAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithTariffsIdDto {
    @Valid
    private EmployeeDto employeeDto;
    @NotEmpty(message = "Tariffs must not be empty")
    private List<TariffWithChatAccess> tariffs;
}
