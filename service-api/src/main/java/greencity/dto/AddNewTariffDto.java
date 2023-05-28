package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
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
    private List<@Min(1) Long> receivingStationsIdList;
}
