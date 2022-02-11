package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class TariffTranslationDto {
    @NotNull
    private String name;
    @NotNull
    private String description;
    @NonNull
    private String nameEng;
}
