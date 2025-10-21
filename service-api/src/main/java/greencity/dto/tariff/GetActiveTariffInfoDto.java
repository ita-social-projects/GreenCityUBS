package greencity.dto.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetActiveTariffInfoDto {
    Long id;
    private String tariffNameUk;
    private String tariffNameEn;
    private String descriptionMessageUk;
    private String descriptionMessageEn;
}
