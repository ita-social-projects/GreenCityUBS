package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailsOfDeactivateTariffsDto {
    private Optional<List<Long>> regionsIds;
    private Optional<List<Long>> citiesIds;
    private Optional<List<Long>> stationsIds;
    private Optional<Long> courierId;
}
