package greencity.dto.courier;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class CreateCourierTranslationDto {
    @NotNull
    private String name;
    @NotNull
    private String nameEn;
}
