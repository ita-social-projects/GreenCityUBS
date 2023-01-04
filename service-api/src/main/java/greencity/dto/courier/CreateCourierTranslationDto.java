package greencity.dto.courier;

import lombok.*;

import javax.validation.constraints.NotNull;

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
