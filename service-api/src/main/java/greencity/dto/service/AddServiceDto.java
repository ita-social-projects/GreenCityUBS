package greencity.dto.service;

import greencity.dto.tariff.TariffTranslationDto;
import lombok.*;
import javax.validation.Valid;
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
    @NotNull
    Integer commission;
    @Valid
    TariffTranslationDto tariffTranslationDtoList;
    @NotNull
    Long locationId;
}
