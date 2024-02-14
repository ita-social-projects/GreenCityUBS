package greencity.dto.violation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
