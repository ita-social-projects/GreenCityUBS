package greencity.dto.user;

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
public class UserAgreementDetailDto extends  UserAgreementDto{
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
