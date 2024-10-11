package greencity.dto.user;

import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ValidationConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String email;

    @NotBlank
    @Pattern(regexp = ValidationConstant.NAME_REGEXP)
    private String firstName;

    @Min(1)
    @Max(1000000)
    private Long id;

    @NotBlank
    @Pattern(regexp = ValidationConstant.NAME_REGEXP)
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
