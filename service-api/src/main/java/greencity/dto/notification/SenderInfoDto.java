package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SenderInfoDto {
    private String senderName;
    private String senderSurname;
    private String senderEmail;
    private String senderPhone;
}