package greencity.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderAddressDtoResponse implements Serializable {
    private String district;
    private String street;
    private String houseCorpus;
    private String entranceNumber;
    private String houseNumber;
}
