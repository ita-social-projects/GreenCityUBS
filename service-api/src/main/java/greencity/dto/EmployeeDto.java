package greencity.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import greencity.entity.enums.EmployeePosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import lombok.*;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EmployeeDto {
    @Min(1)
    private Long id;
    @NotEmpty
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String firstName;
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String lastName;
    @NotEmpty
    private String phoneNumber;
    @Email
    private String email;
    private String image;
    @NotEmpty
    private List<EmployeePosition> employeePositions;
    @NotEmpty
    private List<String> receivingStations;
}
