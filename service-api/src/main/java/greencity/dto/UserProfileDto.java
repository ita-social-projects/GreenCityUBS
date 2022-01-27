package greencity.dto;

import greencity.annotations.ValidPhoneNumber;
import greencity.util.Bot;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
    @Email
    private String recipientEmail;
    @NotBlank
    @ValidPhoneNumber
    private String recipientPhone;
    private List<AddressDto> addressDto;
    private List<Bot> botList;
}