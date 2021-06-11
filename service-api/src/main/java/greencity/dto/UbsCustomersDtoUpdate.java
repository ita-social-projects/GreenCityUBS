package greencity.dto;

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
    private Long id;
    @NotBlank
    @NotNull
    @Length(max = 60)
    private String recipientName;
    @NotBlank
    @NotNull
    @Length(max = 9)
    private String recipientPhoneNumber;
    @NotBlank
    @NotNull
    @Length(max = 50)
    private String recipientEmail;
}
