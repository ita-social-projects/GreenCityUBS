package greencity.dto;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@EqualsAndHashCode
public class AddNewTariffDto {
    @Nullable
    @Min(1)
    private Long regionId;
    @NotNull
    @Min(1)
    private Long courierId;
    @Size(min = 1)
    private List<@Min(1) Long> locationIdList;
    @Nullable
    private List<@Min(1) Long> receivingStationsIdList;
}
