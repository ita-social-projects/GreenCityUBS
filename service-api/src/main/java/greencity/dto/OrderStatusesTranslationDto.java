package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OrderStatusesTranslationDto {
    private String key;
    private String translation;
    private Boolean ableActualChange;
}
