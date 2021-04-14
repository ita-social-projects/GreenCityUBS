package greencity.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
}
