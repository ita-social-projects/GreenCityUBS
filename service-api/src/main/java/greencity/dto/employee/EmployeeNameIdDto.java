package greencity.dto.employee;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EmployeeNameIdDto {
    private String name;
    private Long id;
}
