package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class AddNewTariffDto {
    @NotNull
    @Min(1)
    private Long regionId;
    @NotNull
    @Min(1)
    private Long courierId;
    @NotNull
    private List<@Min(1) Long> locationIdList;
    @NotNull
    private List<@Min(1) Long> receivingStationsIdList;
}
