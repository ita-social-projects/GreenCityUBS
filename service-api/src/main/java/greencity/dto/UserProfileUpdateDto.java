package greencity.dto;

import greencity.annotations.ValidPhoneNumber;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class UserProfileUpdateDto {
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String recipientName;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z\\s-'.]{1,30}")
    private String recipientSurname;
    @NotBlank
    @ValidPhoneNumber
    private String recipientPhone;
    private List<AddressDto> addressDto;
}
