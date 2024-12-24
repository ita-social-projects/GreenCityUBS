package greencity.dto.notification;

import jakarta.validation.constraints.NotEmpty;
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
public class BodyDto {
    @NotEmpty
    private String bodyUa;
    @NotEmpty
    private String bodyEn;
}
