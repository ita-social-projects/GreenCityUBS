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
    private Integer capacity;
    @NotNull
    private Integer price;
    @NotNull
    private Integer commission;
    @Valid
    private TariffTranslationDto tariffTranslationDto;
    @NotNull
    private Long locationId;
}
