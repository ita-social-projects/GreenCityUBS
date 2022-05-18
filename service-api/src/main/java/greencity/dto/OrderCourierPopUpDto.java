package greencity.dto;

import lombok.*;

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
