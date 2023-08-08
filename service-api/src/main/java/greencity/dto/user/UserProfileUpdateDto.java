package greencity.dto.user;

import greencity.annotations.ValidPhoneNumber;
import greencity.dto.address.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(?!\\s*$)[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9ʼ'`ʹ\\s-]{1,30}$")
    private String recipientName;
    @NotBlank
    @Pattern(regexp = "^(?!\\s*$)[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9ʼ'`ʹ\\s-]{1,30}$")
    private String recipientSurname;
    @Email
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")
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
