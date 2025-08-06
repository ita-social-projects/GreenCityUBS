package greencity.dto.employee;

import greencity.annotations.ValidTariffs;
import greencity.dto.tariff.TariffWithChatAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithTariffsIdDto {
    @Valid
    private EmployeeDto employeeDto;
    @ValidTariffs
    private List<TariffWithChatAccess> tariffs;
}
