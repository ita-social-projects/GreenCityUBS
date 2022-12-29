package greencity.dto.courier;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CourierDto {
    private Long courierId;
    private String courierStatus;
    private String nameUk;
    private String nameEn;
    private LocalDate createDate;
    private String createdBy;
}
