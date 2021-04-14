package greencity.dto;

import greencity.entity.order.Bag;

import greencity.entity.order.BagTranslation;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserPointsAndAllBagsDto implements Serializable {
    private List<BagTranslationDto> bags;

    private int points;
}
