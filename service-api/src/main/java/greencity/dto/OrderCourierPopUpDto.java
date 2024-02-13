package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCourierPopUpDto {
    private List<AllActiveLocationsDto> allActiveLocationsDtos;
    private TariffsForLocationDto tariffsForLocationDto;
    private Boolean orderIsPresent;
}
