package greencity.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OrderPaymentStatusesTranslationDto {
    private String key;
    private String ua;
    private String eng;
}
