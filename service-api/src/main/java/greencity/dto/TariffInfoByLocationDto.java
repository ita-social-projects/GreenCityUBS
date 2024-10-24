package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TariffInfoByLocationDto {
    private TariffsForLocationDto tariffsForLocationDto;
    private Boolean orderIsPresent;
}
