package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class AddServiceDto {
    @NotNull
    Integer capacity;
    @NotNull
    Integer price;
    Integer commission;
    List<TariffTranslationDto> tariffTranslationDtoList;
    @NotNull
    Long locationId;
}
