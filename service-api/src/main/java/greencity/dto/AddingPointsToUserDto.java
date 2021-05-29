package greencity.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
