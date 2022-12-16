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
    private Optional<List<Long>> regionsId;
    private Optional<List<Long>> citiesId;
    private Optional<List<Long>> stationsId;
    private Optional<Long> courierId;
}
