package greencity.dto.employee;

import greencity.annotations.ValidPhoneNumber;
import greencity.dto.position.PositionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveEmployeeDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String firstName;
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
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
