package greencity.dto.employee;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import greencity.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEmployeeDto {
    private Long id;
    @NotNull
    @Pattern(regexp = ValidationConstant.NAME_REGEXP, message = ValidationConstant.NAME_VALIDATION_MESSAGE)
    private String firstName;
    @NotNull
    @Pattern(regexp = ValidationConstant.NAME_REGEXP, message = ValidationConstant.NAME_VALIDATION_MESSAGE)
    private String lastName;
    @NotNull
    @ValidPhoneNumber
    private String phoneNumber;
    @NotNull
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String email;
    private EmployeeStatus employeeStatus;
    private String image;
}
