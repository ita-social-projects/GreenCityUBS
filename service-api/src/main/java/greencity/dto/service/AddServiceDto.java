package greencity.dto.service;

import greencity.dto.tariff.TariffTranslationDto;
import lombok.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    TariffTranslationDto tariffTranslationDto;
    @NotNull
    Long locationId;
}
