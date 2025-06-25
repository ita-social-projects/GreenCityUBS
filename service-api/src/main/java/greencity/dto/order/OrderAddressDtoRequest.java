package greencity.dto.order;

import greencity.dto.CreateAddressRequestDto;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.Max;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id", "actual"}, callSuper = false)
@ToString
@SuperBuilder
public class OrderAddressDtoRequest extends CreateAddressRequestDto {
    @Max(1000000)
    private Long id;

    private Boolean actual;
}
