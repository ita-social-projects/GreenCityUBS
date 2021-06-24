package greencity.dto;

import java.util.List;
import java.util.Set;
import lombok.*;

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
