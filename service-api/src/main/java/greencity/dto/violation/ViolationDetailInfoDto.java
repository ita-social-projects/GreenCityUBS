package greencity.dto.violation;

import greencity.enums.ViolationLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
