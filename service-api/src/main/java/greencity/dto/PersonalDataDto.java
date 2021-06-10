package greencity.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
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
    @Pattern(regexp = "[0-9]{9}")
    private String phoneNumber;

    @Length(max = 200)
    private String addressComment;
}
