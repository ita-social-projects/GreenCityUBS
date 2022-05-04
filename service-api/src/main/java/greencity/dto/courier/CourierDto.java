package greencity.dto.courier;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CourierDto {
    private Long courierId;
    private String courierStatus;
    private List<CourierTranslationDto> courierTranslationDtos;
    private LocalDate createDate;
    private String createdBy;
}
