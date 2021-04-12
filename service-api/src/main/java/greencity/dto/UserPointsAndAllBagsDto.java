package greencity.dto;

import greencity.entity.order.Bag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsAndAllBagsDto implements Serializable {
    private List<Bag> bags;
    private int points;
}
