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
public class CourierDto {
    private Long courierId;
    private String courierStatus;
    private List<CourierTranslationDto> courierTranslationDtos;
}
