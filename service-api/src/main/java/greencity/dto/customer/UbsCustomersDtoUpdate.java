package greencity.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
