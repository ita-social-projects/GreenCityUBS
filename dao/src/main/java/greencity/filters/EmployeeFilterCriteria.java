package greencity.filters;

import greencity.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFilterCriteria {
    @NotNull
    private String searchLine;
//    @NotNull
//    private String contact;
    private EmployeeStatus employeeStatus;
    @NotNull
    private List<Long> positions;
    @NotNull
    private List<Long> regions;
    @NotNull
    private List<Long> locations;
    @NotNull
    private List<Long> couriers;
}
