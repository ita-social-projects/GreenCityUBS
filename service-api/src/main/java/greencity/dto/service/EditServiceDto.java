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
public class EditServiceDto {
    @NotNull
    private Integer price;

    @NotNull
    private String name;

    @NotNull
    private String nameEng;

    @NotNull
    private String description;

    @NotNull
    private String descriptionEng;
}
