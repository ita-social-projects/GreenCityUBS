package greencity.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderAddressDtoUpdate implements Serializable {
    @NotNull
    @Min(1)
    private Long id;
    @NotBlank
    @Length(max = 30)
    private String district;
    @Length(min = 3, max = 40)
    @NotNull
    private String street;
    @Length(min = 1, max = 4)
    private String houseCorpus;
    @Length(min = 1, max = 4)
    private String entranceNumber;
    @Length(max = 5)
    private String houseNumber;
}
