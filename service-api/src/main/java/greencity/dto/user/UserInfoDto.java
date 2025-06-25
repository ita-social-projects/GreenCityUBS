package greencity.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDto {
    private Long customerId;
    private String customerName;
    private String customerSurname;
    private String customerPhoneNumber;
    private String customerEmail;
    private String senderName;
    private String senderSurname;
    private String senderPhoneNumber;
    private String senderEmail;
    private int totalUserViolations;
    private int userViolationForCurrentOrder;
}
