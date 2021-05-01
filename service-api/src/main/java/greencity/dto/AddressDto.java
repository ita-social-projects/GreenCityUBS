package greencity.dto;

import greencity.entity.coords.Coordinates;
import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AddressDto implements Serializable {
    @NotNull
    @Min(1)
    private Long id;
    @NotBlank
    @Length(max = 12)
    private String city;
    @NotBlank
    @Length(max = 30)
    private String district;
    @Length(max = 4)
    private String entranceNumber;
    @Length(max = 5)
    private String houseCorpus;
    @Length(max = 5)
    private String houseNumber;
    @Length(max = 50)
    private String street;

    private Coordinates coordinates;

    private Boolean actual;
}
