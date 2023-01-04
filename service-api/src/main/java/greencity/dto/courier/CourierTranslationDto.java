package greencity.dto.courier;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder

public class CourierTranslationDto {
    private Long id;
    private String nameUk;
    private String nameEn;
}
