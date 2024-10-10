package greencity.dto.employee;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import greencity.dto.position.PositionDto;
import greencity.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    @NotNull
    @Pattern(regexp = ValidationConstant.NAME_REGEXP)
    private String firstName;
    @NotNull
    @Pattern(regexp = ValidationConstant.NAME_REGEXP)
    private String lastName;
    @NotNull
    @ValidPhoneNumber
    private String phoneNumber;
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    @NotBlank
    private String email;
    private EmployeeStatus employeeStatus;
    private String image;
    @NotEmpty
    private List<PositionDto> employeePositions;
}
