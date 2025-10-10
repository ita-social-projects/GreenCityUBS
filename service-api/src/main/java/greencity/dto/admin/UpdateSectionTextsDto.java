package greencity.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSectionTextsDto {
    @NotBlank(message = "Field name cannot be empty")
    private String field;

    @NotBlank(message = "Value cannot be empty")
    private String valueUK;

    @NotBlank(message = "Value cannot be empty")
    private String valueEN;
}
