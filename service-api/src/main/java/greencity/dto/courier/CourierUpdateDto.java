package greencity.dto.courier;

import lombok.*;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CourierUpdateDto {
    private Long courierId;
    private List<CourierTranslationDto> courierTranslationDtos;
}
