package greencity.dto.tariff;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class EditTariffServiceDto {
    @NotNull
    String name;
    @NotNull
    Integer capacity;
    @NotNull
    Integer price;
    Integer commission;
    String description;
    @NotNull
    String langCode;
}
