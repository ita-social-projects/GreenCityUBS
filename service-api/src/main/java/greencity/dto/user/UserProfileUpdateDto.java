package greencity.dto.user;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import greencity.dto.address.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class UserProfileUpdateDto implements Serializable {
    @NotBlank
    @Pattern(regexp = ValidationConstant.NAME_REGEXP)
    private String recipientName;
    @Pattern(regexp = "^$|" + ValidationConstant.NAME_REGEXP)
    private String recipientSurname;
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String alternateEmail;
    @ValidPhoneNumber
    private String recipientPhone;
    @Valid
    private List<AddressDto> addressDto;
    @NotNull
    private Boolean telegramIsNotify;
    private Boolean viberIsNotify;
}
