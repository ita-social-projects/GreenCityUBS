package greencity.dto.notification;

import greencity.entity.user.User;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InactiveAccountDto {
    private List<User> users;
    private Long months;
}
