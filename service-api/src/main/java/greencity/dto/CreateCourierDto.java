package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourierDto {
    @NotNull
    List<LimitsDto> createCourierLimitsDto;
    @NotNull
    List<CreateCourierTranslationDto> createCourierTranslationDtos;
}
