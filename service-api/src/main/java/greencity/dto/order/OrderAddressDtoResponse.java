package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderAddressDtoResponse implements Serializable {
    private String districtUk;
    private String districtEn;
    private String streetUk;
    private String streetEn;
    private String houseCorpus;
    private String entranceNumber;
    private String houseNumber;
}
