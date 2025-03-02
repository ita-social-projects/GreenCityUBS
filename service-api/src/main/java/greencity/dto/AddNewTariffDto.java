package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@EqualsAndHashCode
public class AddNewTariffDto {
    @Min(1)
    private Long regionId;
    @NotNull
    @Min(1)
    private Long courierId;
    @NotEmpty
    private List<@Min(1) Long> locationIdList;
    @NotEmpty
    private List<@Min(1) Long> receivingStationsIdList;
}
