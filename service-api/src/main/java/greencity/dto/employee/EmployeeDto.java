package greencity.dto.employee;

import greencity.annotations.ValidPhoneNumber;
import greencity.dto.position.PositionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    @Min(1)
    private Long id;
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]+([._]?[a-zA-Z0-9]+)++$")
    private String firstName;
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]+([._]?[a-zA-Z0-9]+)++$")
    private String lastName;
    @NotNull
    @ValidPhoneNumber
    private String phoneNumber;
    @Email
    private String email;
    private String image;
    @NotEmpty
    private List<PositionDto> employeePositions;
    @NotEmpty
    private List<Long> tariffId;
}
