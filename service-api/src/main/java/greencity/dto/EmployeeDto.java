package greencity.dto;

import greencity.annotations.ValidPhoneNumber;
import greencity.entity.enums.EmployeeStatus;
import lombok.*;

import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EmployeeDto {
    @Min(1)
    private Long id;
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
    @NotNull
    private EmployeeStatus employeeStatus;
    @NotEmpty
    private List<PositionDto> employeePositions;
    @NotEmpty
    private List<ReceivingStationDto> receivingStations;
}
