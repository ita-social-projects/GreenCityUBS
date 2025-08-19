package greencity.dto.employee;

import greencity.annotations.ValidPositions;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateUpdateEmployeeDto extends BaseEmployeeDto {
    @NotEmpty
    @ValidPositions
    @Singular
    private Set<Long> employeePositionIds;
}
