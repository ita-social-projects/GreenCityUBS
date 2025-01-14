package greencity.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserInfoDto {
    private String customerName;
    private String customerSurName;
    private String customerPhoneNumber;
    private String customerEmail;
    private Long recipientId;
    private String recipientName;
    private String recipientSurName;
    private String recipientPhoneNumber;
    private String recipientEmail;
    private int totalUserViolations;
    private int userViolationForCurrentOrder;
}
