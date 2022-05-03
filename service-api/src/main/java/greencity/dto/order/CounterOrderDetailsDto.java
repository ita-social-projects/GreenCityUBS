package greencity.dto.order;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CounterOrderDetailsDto {
    Double totalAmount;
    Double totalConfirmed;
    Double totalExported;
    Double sumAmount;
    Double sumConfirmed;
    Double sumExported;
    Double certificateBonus;
    Double bonus;
    Double totalSumAmount;
    Double totalSumConfirmed;
    Double totalSumExported;
    String orderComment;
    List<String> certificate;
    Set<String> numberOrderFromShop;
}
