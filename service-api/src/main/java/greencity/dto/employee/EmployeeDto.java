package greencity.dto.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import greencity.annotations.ValidPhoneNumber;
import greencity.dto.LocationsDtos;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.TariffsInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @NotEmpty
    private List<PositionDto> employeePositions;
    @NotEmpty
    private List<ReceivingStationDto> receivingStations;
    @NotNull
    private LocationsDtos location;
    @NotNull
    private CourierTranslationDto courier;
    @JsonIgnore
    private Set<TariffsInfoDto> tariffs;
}
