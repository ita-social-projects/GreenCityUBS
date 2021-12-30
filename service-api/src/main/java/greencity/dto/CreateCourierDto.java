package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class CreateCourierDto {
    @NotNull
    List<LimitsDto> createCourierLimitsDto;
    @NotNull
    List<CreateCourierTranslationDto> createCourierTranslationDtos;
}
