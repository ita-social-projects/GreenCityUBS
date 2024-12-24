package greencity.dto.violation;

import com.fasterxml.jackson.annotation.JsonFormat;
import greencity.enums.ViolationLevel;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserViolationsDto {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime violationDate;
    private Long orderId;
    private ViolationLevel violationLevel;
}
