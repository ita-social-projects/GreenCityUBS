package greencity.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFilterCriteria {
    private String searchLine;
    private String employeeStatus;
    private List<Long> positions;
    private List<Long> regions;
    private List<Long> locations;
    private List<Long> couriers;
}
