package greencity.dto;

import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AddEmployeeDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String firstName;
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String lastName;
    @NotNull
    private String phoneNumber;
    @Email
    private String email;
    @NotEmpty
    private List<PositionDto> employeePositions;
    @NotEmpty
    private List<ReceivingStationDto> receivingStations;
}
