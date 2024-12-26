package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String title;
    private String body;
    private List<String> images;
}
