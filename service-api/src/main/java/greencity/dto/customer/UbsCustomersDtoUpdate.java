package greencity.dto.customer;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class UbsCustomersDtoUpdate {
    @NotNull
    @Min(1)
    private Long recipientId;
    @NotNull
    @Length(max = 60)
    @NotBlank
    private String recipientName;
    @NotNull
    @Length(max = 60)
    @NotBlank
    private String recipientSurName;
    @NotNull
    @Length(max = 9)
    @NotBlank
    private String recipientPhoneNumber;
    @NotNull
    @Length(max = 50)
    @NotBlank
    private String recipientEmail;
}
