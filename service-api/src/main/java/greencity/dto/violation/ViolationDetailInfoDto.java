package greencity.dto.violation;

import greencity.entity.enums.ViolationLevel;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ViolationDetailInfoDto {
    private Long orderId;
    private ViolationLevel violationLevel;
    private String description;
    @Nullable
    private List<String> images;
    private ZonedDateTime violationDate;
    private String addedByUser;
}
