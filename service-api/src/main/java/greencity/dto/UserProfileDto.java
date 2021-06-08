package greencity.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
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
    @Email
    private String recipientEmail;
    @NotBlank
    @Pattern(regexp = "[0-9]{9}")
    private String recipientPhone;
    private AddressDto addressDto;
}
