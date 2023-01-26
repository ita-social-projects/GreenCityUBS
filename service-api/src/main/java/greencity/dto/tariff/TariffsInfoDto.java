package greencity.dto.tariff;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeNameDto;
import greencity.entity.order.TariffLocation;
import greencity.enums.CourierLimit;
import greencity.enums.LocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TariffsInfoDto {
    private Long id;
    private Set<TariffLocation> tariffLocations;
    private LocationStatus locationStatus;
    private List<ReceivingStationDto> receivingStations;
    private CourierDto courier;
    private String limitDescription;
    private Long min;
    private Long max;
    private CourierLimit courierLimit;
    private EmployeeNameDto creator;
    private LocalDate createdAt;
}
