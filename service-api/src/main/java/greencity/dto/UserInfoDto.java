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
    private String customerSurName;
    private String customerPhoneNumber;
    private String customerEmail;
    private String recipientName;
    private String recipientSurName;
    private String recipientPhoneNumber;
    private String recipientEmail;
    private int totalUserViolations;
    private int userViolationForCurrentOrder;
}
