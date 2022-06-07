package greencity.dto.useraction;

import greencity.entity.enums.UserActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserActionMessage {
    private String userEmail;
    private UserActionType actionType;
    private Long actionId;
    private String timestamp;
}
