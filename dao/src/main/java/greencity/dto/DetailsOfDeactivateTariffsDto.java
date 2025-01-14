package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@Builder
public class DetailsOfDeactivateTariffsDto {
    private Optional<List<Long>> regionsIds;
    private Optional<List<Long>> citiesIds;
    private Optional<List<Long>> stationsIds;
    private Optional<Long> courierId;
    @NonNull
    private String activationStatus;
}
