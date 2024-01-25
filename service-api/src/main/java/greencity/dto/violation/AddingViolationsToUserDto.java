package greencity.dto.violation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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
