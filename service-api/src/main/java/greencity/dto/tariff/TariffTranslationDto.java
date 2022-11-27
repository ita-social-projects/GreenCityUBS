package greencity.dto.tariff;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class TariffTranslationDto {
    @NotNull
    @NotEmpty
    private String name;
    @NonNull
    @NotEmpty
    private String nameEng;
    @NotNull
    @NotEmpty
    private String description;
    @NonNull
    @NotEmpty
    private String descriptionEng;
}
