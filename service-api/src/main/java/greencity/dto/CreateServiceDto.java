package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateServiceDto {
    @NotNull
    String name;
    Integer capacity;
    @NotNull
    Integer basePrice;
    Integer commission;
    String description;
}
