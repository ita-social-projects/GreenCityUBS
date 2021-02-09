package greencity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedCoordinatesDto {
    private Integer amountOfLitres;
    private List<CoordinatesDto> groupOfCoordinates;
}