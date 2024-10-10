package greencity.dto.user;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import greencity.dto.address.AddressDto;
import greencity.util.Bot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class UserProfileDto {
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String recipientName;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z\\s-'.]{1,30}")
    private String recipientSurname;
    @NotBlank
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String recipientEmail;
    @NotBlank
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String alternateEmail;
    @NotBlank
    @ValidPhoneNumber
    private String recipientPhone;
    private List<AddressDto> addressDto;
    private List<Bot> botList;
    private Boolean hasPassword;
    @NonNull
    private Boolean telegramIsNotify;
    @NonNull
    private Boolean viberIsNotify;
}