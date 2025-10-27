package greencity.dto.notification;

import greencity.dto.user.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InactiveAccountDto {
    private List<UserProfileDto> users;
    private Long months;
}
