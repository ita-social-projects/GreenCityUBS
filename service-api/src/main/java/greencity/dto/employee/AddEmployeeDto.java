package greencity.dto.employee;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.position.PositionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEmployeeDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String firstName;
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String lastName;
    @NotNull
    @ValidPhoneNumber
    private String phoneNumber;
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String email;
    @NotEmpty
    private List<PositionDto> employeePositions;
    @NotEmpty
    private List<ReceivingStationDto> receivingStations;
}
