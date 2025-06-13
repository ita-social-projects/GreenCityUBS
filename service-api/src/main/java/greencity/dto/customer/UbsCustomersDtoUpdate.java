package greencity.dto.customer;

import greencity.annotations.ValidPhoneNumber;
import jakarta.validation.constraints.Email;
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
    private Long customerId;
    @NotNull
    @Length(max = 60)
    @NotBlank
    private String customerName;
    @NotNull
    @Length(max = 60)
    @NotBlank
    private String customerSurname;
    @NotNull
    @NotBlank
    @ValidPhoneNumber
    private String customerPhoneNumber;
    @NotNull
    @NotBlank
    @Email
    @Length(max = 255)
    private String customerEmail;
}
