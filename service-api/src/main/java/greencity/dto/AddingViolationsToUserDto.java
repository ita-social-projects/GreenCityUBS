package greencity.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
