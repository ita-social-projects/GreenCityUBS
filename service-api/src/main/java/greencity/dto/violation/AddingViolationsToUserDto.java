package greencity.dto.violation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddingViolationsToUserDto {
    @NotNull
    @Min(1)
    @Max(10000000)
    private Long orderID;
    @NotNull
    @Length(min = 5, max = 300)
    private String violationDescription;
    @NotNull
    private String violationLevel;
}
