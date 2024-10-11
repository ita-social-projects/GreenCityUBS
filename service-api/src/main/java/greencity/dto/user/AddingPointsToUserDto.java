package greencity.dto.user;

import greencity.constant.ValidationConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddingPointsToUserDto {
    @NotNull
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String email;
    @NotNull
    @Min(1)
    @Max(10000)
    private int additionalPoints;
}
