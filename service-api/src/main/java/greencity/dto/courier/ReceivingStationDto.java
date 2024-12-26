package greencity.dto.courier;

import greencity.enums.StationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingStationDto {
    @Min(1)
    private Long id;
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9-'\\s.]{1,30}")
    private String name;

    private String createdBy;

    private LocalDate createDate;

    private StationStatus stationStatus;
}
