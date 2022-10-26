package greencity.dto.tariff;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.entity.order.TariffLocation;
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
    private Long minAmountOfBags;
    private Long maxAmountOfBags;
    private Long minPriceOfOrder;
    private Long maxPriceOfOrder;
    private String creator;
    private LocalDate createdAt;
}
