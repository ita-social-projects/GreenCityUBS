package greencity.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeNameDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
}
