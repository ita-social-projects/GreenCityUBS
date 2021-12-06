package greencity.filters;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFilterCriteria {
    private String[] employeePositions;
    private String[] receivingStations;
    private String search;
}
