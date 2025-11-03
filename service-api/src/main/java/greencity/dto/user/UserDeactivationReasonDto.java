package greencity.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDeactivationReasonDto {
    private String email;
    private String name;
    private String reason;
    private String lang;
}
