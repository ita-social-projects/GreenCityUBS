package greencity.dto;

import greencity.entity.enums.ViolationLevel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ViolationDetailInfoDto {
    private Long orderId;
    private String userName;
    private ViolationLevel violationLevel;
    private String description;
    private String image;
    private LocalDateTime violationDate;

}
