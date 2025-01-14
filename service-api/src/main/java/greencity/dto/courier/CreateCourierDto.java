package greencity.dto.courier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
