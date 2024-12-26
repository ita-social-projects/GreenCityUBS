package greencity.dto.user;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatLinkDto {
    private Long userId;
    @Length(max = 255)
    @NotNull
    @Pattern(regexp = "^$|^https://my.binotel.ua.*", message = "Link must start with 'https://my.binotel.ua'")
    private String link;
}
