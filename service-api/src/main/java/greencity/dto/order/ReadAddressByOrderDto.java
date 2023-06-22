package greencity.dto.order;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode

public class ReadAddressByOrderDto implements Serializable {
    @Length(max = 30)
    private String district;
    @Length(max = 4)
    private String entranceNumber;
    @Length(max = 5)
    private String houseCorpus;
    @Length(max = 10)
    private String houseNumber;
    @Length(max = 50)
    private String street;
    @Length(max = 200)
    private String comment;
}
