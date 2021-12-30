package greencity.dto;

import lombok.*;

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
