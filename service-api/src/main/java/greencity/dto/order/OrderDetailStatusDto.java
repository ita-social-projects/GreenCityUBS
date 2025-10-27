package greencity.dto.order;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailStatusDto {
    Long id;
    String orderStatus;
    String paymentStatus;
    LocalDate date;
}
