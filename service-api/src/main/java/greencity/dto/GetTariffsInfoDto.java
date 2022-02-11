package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
}
