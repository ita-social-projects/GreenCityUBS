package greencity.filters;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFilterCriteria {
    private String[] employeePositions;
    private String[] receivingStations;
    private String search;
}
