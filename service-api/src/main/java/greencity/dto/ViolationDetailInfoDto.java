package greencity.dto;

import greencity.entity.enums.ViolationLevel;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
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
    private LocalDateTime violationDate;
    private String addedByUser;
}
