package greencity.dto.user;

import greencity.dto.bag.BagTranslationDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserPointsAndAllBagsDto implements Serializable {
    private List<BagTranslationDto> bags;
    private long points;
}