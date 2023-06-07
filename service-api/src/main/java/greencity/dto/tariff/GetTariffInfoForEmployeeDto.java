package greencity.dto.tariff;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.GetReceivingStationDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetTariffInfoForEmployeeDto {
    private Long id;
    private RegionDto region;
    private List<LocationsDtos> locationsDtos;
    private List<GetReceivingStationDto> receivingStationDtos;
    private CourierTranslationDto courier;
}
