package greencity.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DeactivateUserRequestDto {
    @NotBlank
    @NonNull
    private String reason;
}
