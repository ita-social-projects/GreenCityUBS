package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class BagMappingDto {
    Integer amount;
    Integer confirmed;
    Integer exported;
}
