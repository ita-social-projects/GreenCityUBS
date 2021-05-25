package greencity.dto;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class UserViolationMailDto {
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String language;
    private String violationDescription;
}
