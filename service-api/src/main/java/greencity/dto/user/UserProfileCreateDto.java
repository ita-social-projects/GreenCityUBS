package greencity.dto.user;

import greencity.constant.ValidationConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class UserProfileCreateDto {
    @NotBlank
    private String uuid;
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String email;
    @Pattern(regexp = ValidationConstant.USERNAME_REGEXP,
        message = ValidationConstant.USERNAME_MESSAGE)
    private String name;
}
