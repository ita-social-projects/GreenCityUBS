package greencity.dto.service;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EditServiceDto {
    @NotNull
    Integer price;
    @NotNull
    String name;
    @NotNull
    String nameEng;
    String description;
    String descriptionEng;
}
