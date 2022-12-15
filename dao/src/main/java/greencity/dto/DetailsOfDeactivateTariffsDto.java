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
    Optional<List<Long>> regionsId;
    Optional<List<Long>> citiesId;
    Optional<List<Long>> stationsId;
    Optional<Long> courierId;
}
