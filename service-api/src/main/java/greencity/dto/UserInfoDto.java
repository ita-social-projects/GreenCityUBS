package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserInfoDto {
    private String customerName;
    private String customerPhoneNumber;
    private String customerEmail;
    private String recipientName;
    private String recipientPhoneNumber;
    private String recipientEmail;
    private int totalUserViolations;
    private int userViolationForCurrentOrder;
}
