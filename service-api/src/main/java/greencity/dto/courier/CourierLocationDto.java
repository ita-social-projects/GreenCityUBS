package greencity.dto.courier;

import greencity.dto.location.LocationTranslationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CourierLocationDto {
    private Long locationId;
    private List<LocationTranslationDto> locationTranslationDtoList;
}
