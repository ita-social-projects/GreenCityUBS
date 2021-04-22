package greencity.dto;

import javax.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class UbsCustomersDto {
    private String name;
    private String email;
    private String phoneNumber;
}
