package greencity.dto.useragreement;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

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
