package greencity.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddingPointsToUserDto {
    @NotNull
    @Email
    private String email;
    @NotNull
    @Min(1)
    @Max(10000)
    private int additionalPoints;
}
