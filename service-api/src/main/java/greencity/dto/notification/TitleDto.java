package greencity.dto.notification;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleDto {
    @NotEmpty
    private String enTitle;
    @NotEmpty
    private String uaTitle;
}
