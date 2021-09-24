package greencity.dto;

import greencity.annotations.ValidPhoneNumber;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "email")
@ToString
public class PersonalDataDto implements Serializable {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String firstName;
    @Min(1)
    @Max(1000000)
    private Long id;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z\\s-'.]{1,30}")
    private String lastName;
    @NotBlank
    @ValidPhoneNumber
    private String phoneNumber;

    @Length(max = 200)
    private String addressComment;
}
