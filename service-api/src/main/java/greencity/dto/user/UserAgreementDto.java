package greencity.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class UserAgreementDto {
    @NotEmpty
    private String textUa;
    @NotEmpty
    private String textEn;
}
