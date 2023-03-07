package greencity.dto.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class EditTariffDto {
    @NotEmpty
    private List<@Min(1) Long> locationIds;
    @NotEmpty
    private List<@Min(1) Long> receivingStationIds;
}
