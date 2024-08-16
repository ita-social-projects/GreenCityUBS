package greencity.dto.useragreement;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserAgreementDetailDto extends UserAgreementDto {
    @NotNull
    private Long id;
    @NotNull
    private LocalDateTime createdAt;
    @NotEmpty
    private String authorEmail;
}
