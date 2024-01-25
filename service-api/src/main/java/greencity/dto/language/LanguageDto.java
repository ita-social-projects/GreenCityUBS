package greencity.dto.language;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"id", "code"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageDto {
    @Min(1)
    private Long id;

    @NotNull
    private String code;
}
