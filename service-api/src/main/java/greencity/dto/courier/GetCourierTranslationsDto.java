package greencity.dto.courier;

import greencity.dto.courier.CourierDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class GetCourierTranslationsDto {
    private Long id;
    private Long locationId;
    private String name;
    private String languageCode;
    private String limitDescription;
    List<CourierDto> couriersLimit;
}
