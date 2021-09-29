package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GetTariffServiceDto {
    @NotNull
    String name;
    @NotNull
    Integer capacity;
    @NotNull
    Integer price;
    Integer commission;
    String description;
    String languageCode;
    Integer fullPrice;
    Integer id;
}
