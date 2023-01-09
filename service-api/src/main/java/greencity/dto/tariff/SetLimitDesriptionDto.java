package greencity.dto.tariff;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class SetLimitDesriptionDto {
    private String limitDescription;
    private Integer tariffId;
}
