package greencity.dto.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserProfileCreateDto {
    @NotBlank
    private String uuid;
    @Email
    private String email;
    @Size(min = 1, max = 30, message = "name must have no less than 1 and no more than 30 symbols")
    @Pattern(regexp = "^(?!\\.)(?!.*\\.$)(?!.*?\\.\\.)(?!.*?\\-\\-)(?!.*?\\'\\')[-'ʼ ґҐіІєЄїЇА-Яа-я+\\w.]{1,30}$",
        message = "name must contain only \"ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'0-9 .\", dot can only be in the center of the name")
    private String name;
}
