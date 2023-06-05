package greencity.dto.employee;

import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class GetEmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String image;
    private List<PositionDto> employeePositions;
    private List<GetTariffInfoForEmployeeDto> tariffs;
}
