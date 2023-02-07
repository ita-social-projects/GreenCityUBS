package greencity.dto.courier;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static greencity.constant.ValidationConstant.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class CreateCourierDto {
    @NotNull
    @Pattern(regexp = COURIER_NAME_EN_REGEXP, message = COURIER_NAME_EN_MESSAGE)
    private String nameEn;

    @NotNull
    @Pattern(regexp = COURIER_NAME_UK_REGEXP, message = COURIER_NAME_UK_MESSAGE)
    private String nameUk;
}
