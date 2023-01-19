package greencity.dto.tariff;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeNameDto;
import greencity.enums.LocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTariffsInfoDto {
    private Long cardId;
    private RegionDto regionDto;
    private CourierDto courierDto;
    private List<LocationsDtos> locationInfoDtos;
    private List<ReceivingStationDto> receivingStationDtos;
    private LocationStatus tariffStatus;
    private String limitDescription;
    private EmployeeNameDto creator;
    private LocalDate createdAt;
    private String courierLimit;
    private Long minQuantity;
    private Long maxQuantity;
}
