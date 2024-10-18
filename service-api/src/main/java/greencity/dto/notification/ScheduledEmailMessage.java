package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledEmailMessage {
    private String username;
    private String email;
    private String baseLink;
    private String subject;
    private String body;
    private String language;
    private boolean isUbs;
}
