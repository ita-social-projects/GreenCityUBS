package greencity.dto;

import lombok.*;

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