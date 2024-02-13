package greencity.dto.user;

import greencity.annotations.ValidPhoneNumber;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
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
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ0-9\\s-ʼ'`ʹ]{1,30}")
    private String firstName;

    @Min(1)
    @Max(1000000)
    private Long id;

    @NotBlank
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ0-9\\s-ʼ'`ʹ]{1,30}")
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
