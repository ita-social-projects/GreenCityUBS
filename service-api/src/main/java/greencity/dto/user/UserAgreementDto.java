package greencity.dto.user;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class UserAgreementDto {
    private String textUa;
    private String textEn;
}
