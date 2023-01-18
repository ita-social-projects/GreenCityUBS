package greencity.dto.service;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GetServiceDto {
    Long id;

    @NotNull
    Integer price;

    @NotNull
    String name;

    @NotNull
    String nameEng;

    String description;

    String descriptionEng;
}
