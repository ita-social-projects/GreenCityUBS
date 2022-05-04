package greencity.dto.violation;

import lombok.*;

import javax.validation.constraints.NotNull;

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
