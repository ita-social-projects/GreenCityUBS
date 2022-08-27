package greencity.dto.violation;

import com.fasterxml.jackson.annotation.JsonFormat;
import greencity.entity.enums.ViolationLevel;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserViolationsDto {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private ZonedDateTime violationDate;
    private Long orderId;
    private ViolationLevel violationLevel;
}
