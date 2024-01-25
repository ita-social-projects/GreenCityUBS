package greencity.dto.location;

import greencity.constant.ErrorMessage;
import greencity.constant.ValidationConstant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EditLocationDto {
    @NotNull
    @Min(1)
    private Long locationId;
    @Pattern(regexp = ValidationConstant.CITY_EN_REGEXP, message = ErrorMessage.USE_ONLY_ENGLISH_LETTERS)
    @Size(min = 3, max = 40, message = ErrorMessage.CITY_NAME_CHARACTER_LIMIT)
    private String nameEn;
    @Pattern(regexp = ValidationConstant.CITY_UK_REGEXP, message = ErrorMessage.USE_ONLY_UKRAINIAN_LETTERS)
    @Size(min = 3, max = 40, message = ErrorMessage.CITY_NAME_CHARACTER_LIMIT)
    private String nameUa;
}
