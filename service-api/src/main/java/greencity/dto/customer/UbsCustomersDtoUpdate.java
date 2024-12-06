package greencity.dto.customer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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
