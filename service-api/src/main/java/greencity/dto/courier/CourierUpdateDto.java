package greencity.dto.courier;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CourierUpdateDto {
    private Long courierId;
    private String nameEn;
    private String nameUk;
}
