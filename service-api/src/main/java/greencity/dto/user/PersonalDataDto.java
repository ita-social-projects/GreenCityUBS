package greencity.dto.user;

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
@EqualsAndHashCode
@ToString
public class PersonalDataDto implements Serializable {
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
    private String senderFirstName;
    private String senderLastName;
    private String senderEmail;
    private String senderPhoneNumber;

    @Length(max = 255)
    private String addressComment;

    private Long ubsUserId;
}
