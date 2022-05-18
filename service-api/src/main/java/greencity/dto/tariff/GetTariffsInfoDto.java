package greencity.dto.tariff;

import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.LocationInfoDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class GetTariffsInfoDto {
    private Long cardId;
    private LocationInfoDto locationInfoDto;
    private ReceivingStationDto receivingStationDto;
    private List<CourierTranslationDto> courierTranslationDtos;
    private String locationStatus;
    private String creator;
    private LocalDate createdAt;
}
