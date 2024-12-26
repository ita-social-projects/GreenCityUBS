package greencity.dto.user;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import greencity.dto.address.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

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
    @NotBlank
    @Pattern(regexp = ValidationConstant.NAME_REGEXP)
    private String recipientSurname;
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String alternateEmail;
    @NotBlank
    @ValidPhoneNumber
    private String recipientPhone;
    @Valid
    private List<AddressDto> addressDto;
    @NonNull
    private Boolean telegramIsNotify;
    @NonNull
    private Boolean viberIsNotify;
}
